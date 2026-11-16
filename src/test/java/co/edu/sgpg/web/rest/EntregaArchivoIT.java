package co.edu.sgpg.web.rest;

import static co.edu.sgpg.security.AuthoritiesConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import co.edu.sgpg.IntegrationTest;
import co.edu.sgpg.domain.*;
import co.edu.sgpg.domain.enumeration.*;
import co.edu.sgpg.repository.*;
import jakarta.persistence.EntityManager;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/** Persistencia BYTEA real, contrato JSON y permisos de descarga con PostgreSQL/Testcontainers. */
@IntegrationTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "pdf-estudiante", authorities = ESTUDIANTE)
class EntregaArchivoIT {

    private static final byte[] PDF = "%PDF-1.7\n1 0 obj\n<<>>\nendobj\n%%EOF\n".getBytes(StandardCharsets.US_ASCII);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private EntityManager em;

    @Autowired
    private EntregaRepository entregas;

    @Autowired
    private ProyectoGradoRepository proyectos;

    @Autowired
    private UserRepository usuarios;

    private ProyectoGrado proyecto;

    @BeforeEach
    void preparar() {
        User estudiante = new User();
        estudiante.setLogin("pdf-estudiante");
        estudiante.setPassword("x".repeat(60));
        estudiante.setActivated(true);
        estudiante = usuarios.saveAndFlush(estudiante);
        proyecto = proyectos.saveAndFlush(
            new ProyectoGrado()
                .titulo("Proyecto PDF")
                .resumen("Resumen")
                .programa("Sistemas")
                .estado(EstadoProyecto.ACTIVO)
                .etapaActual(Etapa.PROPUESTA)
                .porcentajeAvance(0)
                .fechaRegistro(Instant.EPOCH)
                .addEstudiante(estudiante)
        );
    }

    private Map<String, Object> cuerpo(byte[] contenido) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("titulo", "Documento de propuesta");
        cuerpo.put("proyecto", Map.of("id", proyecto.getId()));
        if (contenido != null) {
            cuerpo.put("archivo", contenido);
            cuerpo.put("archivoContentType", "application/pdf");
            cuerpo.put("archivoNombre", "Propuesta académica.pdf");
        }
        return cuerpo;
    }

    private long subirPdf() throws Exception {
        var resultado = mvc
            .perform(
                post("/api/entregas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(cuerpo(PDF)))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.archivo").doesNotExist())
            .andExpect(jsonPath("$.tieneArchivo").value(true))
            .andExpect(jsonPath("$.archivoNombre").value("Propuesta académica.pdf"))
            .andExpect(jsonPath("$.archivoContentType").value("application/pdf"))
            .andReturn();
        return om.readTree(resultado.getResponse().getContentAsByteArray()).get("id").asLong();
    }

    @Test
    void subeYLeeUnPdfComoByteaSinIncluirBytesEnJson() throws Exception {
        long id = subirPdf();
        em.flush();
        em.clear();
        assertThat(entregas.findById(id).orElseThrow().getArchivo()).isEqualTo(PDF);
        assertThat(
            em
                .createNativeQuery(
                    "select data_type from information_schema.columns where table_schema = current_schema() " +
                        "and table_name = 'entrega' and column_name = 'archivo'",
                    String.class
                )
                .getSingleResult()
        ).isEqualTo("bytea");
        mvc.perform(get("/api/entregas/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.archivo").doesNotExist())
            .andExpect(jsonPath("$.tieneArchivo").value(true));
        mvc.perform(get("/api/entregas").param("proyectoId.equals", proyecto.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.[*].archivo").doesNotExist())
            .andExpect(jsonPath("$[0].tieneArchivo").value(true));
        mvc.perform(get("/api/proyecto-grados/{id}/seguimiento", proyecto.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.etapas[0].entregas[0].tieneArchivo").value(true));
    }

    @Test
    void descargaComoEstudianteDelProyectoConNombreUtf8() throws Exception {
        long id = subirPdf();
        em.flush();
        em.clear();
        mvc.perform(get("/api/entregas/{id}/archivo", id))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_PDF))
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''Propuesta%20acad%C3%A9mica.pdf"))
            .andExpect(content().bytes(PDF));
    }

    @Test
    void juradoNoAsignadoNoPuedeDescargar() throws Exception {
        long id = subirPdf();
        mvc.perform(get("/api/entregas/{id}/archivo", id).with(user("pdf-jurado-ajeno").authorities(() -> JURADO))).andExpect(
            status().isForbidden()
        );
    }

    @Test
    void rechazaNoPdfDisfrazadoCon409() throws Exception {
        mvc.perform(
            post("/api/entregas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(cuerpo("texto falso".getBytes(StandardCharsets.UTF_8))))
        )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.codigo").value("ARCHIVO_NO_PDF"));
    }

    @Test
    void rechazaPdfConMimeIncorrecto() throws Exception {
        Map<String, Object> cuerpo = cuerpo(PDF);
        cuerpo.put("archivoContentType", "text/plain");
        mvc.perform(post("/api/entregas").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(cuerpo)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.codigo").value("ARCHIVO_NO_PDF"));
    }

    @Test
    void jsonDeMasDeDiezMbLlegaALaReglaDeTamano() throws Exception {
        byte[] grande = Arrays.copyOf(PDF, 10 * 1024 * 1024 + 1);
        mvc.perform(
            post("/api/entregas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(cuerpo(grande)))
        )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.codigo").value("ARCHIVO_DEMASIADO_GRANDE"));
    }

    @Test
    void sinDocumentoDevuelve409() throws Exception {
        Map<String, Object> cuerpo = cuerpo(null);
        cuerpo.put("urlDocumento", "   ");
        mvc.perform(post("/api/entregas").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(cuerpo)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.codigo").value("DOCUMENTO_REQUERIDO"));
    }

    @Test
    void descarga404SiNoExisteEntregaOArchivo() throws Exception {
        Map<String, Object> cuerpo = cuerpo(null);
        cuerpo.put("urlDocumento", "https://example.org/documento.pdf");
        var resultado = mvc
            .perform(post("/api/entregas").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(cuerpo)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.tieneArchivo").value(false))
            .andReturn();
        long id = om.readTree(resultado.getResponse().getContentAsByteArray()).get("id").asLong();
        mvc.perform(get("/api/entregas/{id}/archivo", id)).andExpect(status().isNotFound());
        mvc.perform(get("/api/entregas/{id}/archivo", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(strings = { "put", "patch" })
    void editarValidaElReemplazoYConservaPdfSiNoSeEnvia(String metodo) throws Exception {
        long id = subirPdf();
        Map<String, Object> cuerpo = cuerpo("falso".getBytes(StandardCharsets.UTF_8));
        cuerpo.put("id", id);
        var peticion = metodo.equals("put") ? put("/api/entregas/{id}", id) : patch("/api/entregas/{id}", id);
        mvc.perform(peticion.contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(cuerpo)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.codigo").value("ARCHIVO_NO_PDF"));
        cuerpo = cuerpo(null);
        cuerpo.put("id", id);
        peticion = metodo.equals("put") ? put("/api/entregas/{id}", id) : patch("/api/entregas/{id}", id);
        mvc.perform(peticion.contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(cuerpo)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tieneArchivo").value(true))
            .andExpect(jsonPath("$.archivoNombre").value("Propuesta académica.pdf"));
        em.flush();
        em.clear();
        mvc.perform(get("/api/entregas/{id}/archivo", id)).andExpect(status().isOk()).andExpect(content().bytes(PDF));
    }
}
