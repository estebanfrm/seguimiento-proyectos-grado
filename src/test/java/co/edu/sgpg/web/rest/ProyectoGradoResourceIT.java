package co.edu.sgpg.web.rest;

import static co.edu.sgpg.domain.ProyectoGradoAsserts.*;
import static co.edu.sgpg.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import co.edu.sgpg.IntegrationTest;
import co.edu.sgpg.domain.ProyectoGrado;
import co.edu.sgpg.domain.User;
import co.edu.sgpg.domain.enumeration.EstadoProyecto;
import co.edu.sgpg.domain.enumeration.Etapa;
import co.edu.sgpg.repository.ProyectoGradoRepository;
import co.edu.sgpg.repository.UserRepository;
import co.edu.sgpg.service.ProyectoGradoService;
import co.edu.sgpg.service.dto.ProyectoGradoDTO;
import co.edu.sgpg.service.mapper.ProyectoGradoMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/**
 * Integration tests for the {@link ProyectoGradoResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class ProyectoGradoResourceIT {

    private static final String DEFAULT_TITULO = "AAAAAAAAAA";
    private static final String UPDATED_TITULO = "BBBBBBBBBB";

    private static final String DEFAULT_RESUMEN = "AAAAAAAAAA";
    private static final String UPDATED_RESUMEN = "BBBBBBBBBB";

    private static final String DEFAULT_PROGRAMA = "AAAAAAAAAA";
    private static final String UPDATED_PROGRAMA = "BBBBBBBBBB";

    private static final Etapa DEFAULT_ETAPA_ACTUAL = Etapa.PROPUESTA;
    private static final Etapa UPDATED_ETAPA_ACTUAL = Etapa.ANTEPROYECTO;

    private static final EstadoProyecto DEFAULT_ESTADO = EstadoProyecto.ACTIVO;
    private static final EstadoProyecto UPDATED_ESTADO = EstadoProyecto.APROBADO;

    private static final Integer DEFAULT_PORCENTAJE_AVANCE = 0;
    private static final Integer UPDATED_PORCENTAJE_AVANCE = 1;
    private static final Integer SMALLER_PORCENTAJE_AVANCE = 0 - 1;

    private static final Instant DEFAULT_FECHA_REGISTRO = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_FECHA_REGISTRO = Instant.ofEpochMilli(1701983268847L);

    private static final Instant DEFAULT_FECHA_ACTUALIZACION = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_FECHA_ACTUALIZACION = Instant.ofEpochMilli(1701983268847L);

    private static final String ENTITY_API_URL = "/api/proyecto-grados";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ProyectoGradoRepository proyectoGradoRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private ProyectoGradoRepository proyectoGradoRepositoryMock;

    @Autowired
    private ProyectoGradoMapper proyectoGradoMapper;

    @Mock
    private ProyectoGradoService proyectoGradoServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restProyectoGradoMockMvc;

    private ProyectoGrado proyectoGrado;

    private ProyectoGrado insertedProyectoGrado;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ProyectoGrado createEntity() {
        return new ProyectoGrado()
            .titulo(DEFAULT_TITULO)
            .resumen(DEFAULT_RESUMEN)
            .programa(DEFAULT_PROGRAMA)
            .etapaActual(DEFAULT_ETAPA_ACTUAL)
            .estado(DEFAULT_ESTADO)
            .porcentajeAvance(DEFAULT_PORCENTAJE_AVANCE)
            .fechaRegistro(DEFAULT_FECHA_REGISTRO)
            .fechaActualizacion(DEFAULT_FECHA_ACTUALIZACION);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ProyectoGrado createUpdatedEntity() {
        return new ProyectoGrado()
            .titulo(UPDATED_TITULO)
            .resumen(UPDATED_RESUMEN)
            .programa(UPDATED_PROGRAMA)
            .etapaActual(UPDATED_ETAPA_ACTUAL)
            .estado(UPDATED_ESTADO)
            .porcentajeAvance(UPDATED_PORCENTAJE_AVANCE)
            .fechaRegistro(UPDATED_FECHA_REGISTRO)
            .fechaActualizacion(UPDATED_FECHA_ACTUALIZACION);
    }

    @BeforeEach
    void initTest() {
        proyectoGrado = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedProyectoGrado != null) {
            proyectoGradoRepository.delete(insertedProyectoGrado);
            insertedProyectoGrado = null;
        }
    }

    @Test
    @Transactional
    void createProyectoGrado() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the ProyectoGrado
        ProyectoGradoDTO proyectoGradoDTO = proyectoGradoMapper.toDto(proyectoGrado);
        var returnedProyectoGradoDTO = om.readValue(
            restProyectoGradoMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(proyectoGradoDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ProyectoGradoDTO.class
        );

        // Validate the ProyectoGrado in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedProyectoGrado = proyectoGradoMapper.toEntity(returnedProyectoGradoDTO);
        assertProyectoGradoUpdatableFieldsEquals(returnedProyectoGrado, getPersistedProyectoGrado(returnedProyectoGrado));

        insertedProyectoGrado = returnedProyectoGrado;
    }

    @Test
    @Transactional
    void createProyectoGradoWithExistingId() throws Exception {
        // Create the ProyectoGrado with an existing ID
        proyectoGrado.setId(1L);
        ProyectoGradoDTO proyectoGradoDTO = proyectoGradoMapper.toDto(proyectoGrado);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restProyectoGradoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(proyectoGradoDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ProyectoGrado in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTituloIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        proyectoGrado.setTitulo(null);

        // Create the ProyectoGrado, which fails.
        ProyectoGradoDTO proyectoGradoDTO = proyectoGradoMapper.toDto(proyectoGrado);

        restProyectoGradoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(proyectoGradoDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkProgramaIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        proyectoGrado.setPrograma(null);

        // Create the ProyectoGrado, which fails.
        ProyectoGradoDTO proyectoGradoDTO = proyectoGradoMapper.toDto(proyectoGrado);

        restProyectoGradoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(proyectoGradoDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkEtapaActualIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        proyectoGrado.setEtapaActual(null);

        // Create the ProyectoGrado, which fails.
        ProyectoGradoDTO proyectoGradoDTO = proyectoGradoMapper.toDto(proyectoGrado);

        restProyectoGradoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(proyectoGradoDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkEstadoIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        proyectoGrado.setEstado(null);

        // Create the ProyectoGrado, which fails.
        ProyectoGradoDTO proyectoGradoDTO = proyectoGradoMapper.toDto(proyectoGrado);

        restProyectoGradoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(proyectoGradoDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkFechaRegistroIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        proyectoGrado.setFechaRegistro(null);

        // Create the ProyectoGrado, which fails.
        ProyectoGradoDTO proyectoGradoDTO = proyectoGradoMapper.toDto(proyectoGrado);

        restProyectoGradoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(proyectoGradoDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllProyectoGrados() throws Exception {
        // Initialize the database
        insertedProyectoGrado = proyectoGradoRepository.saveAndFlush(proyectoGrado);

        // Get all the proyectoGradoList
        restProyectoGradoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(proyectoGrado.getId().intValue())))
            .andExpect(jsonPath("$.[*].titulo").value(hasItem(DEFAULT_TITULO)))
            .andExpect(jsonPath("$.[*].resumen").value(hasItem(DEFAULT_RESUMEN)))
            .andExpect(jsonPath("$.[*].programa").value(hasItem(DEFAULT_PROGRAMA)))
            .andExpect(jsonPath("$.[*].etapaActual").value(hasItem(DEFAULT_ETAPA_ACTUAL.toString())))
            .andExpect(jsonPath("$.[*].estado").value(hasItem(DEFAULT_ESTADO.toString())))
            .andExpect(jsonPath("$.[*].porcentajeAvance").value(hasItem(DEFAULT_PORCENTAJE_AVANCE)))
            .andExpect(jsonPath("$.[*].fechaRegistro").value(hasItem(DEFAULT_FECHA_REGISTRO.toString())))
            .andExpect(jsonPath("$.[*].fechaActualizacion").value(hasItem(DEFAULT_FECHA_ACTUALIZACION.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllProyectoGradosWithEagerRelationshipsIsEnabled() throws Exception {
        when(proyectoGradoServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restProyectoGradoMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(proyectoGradoServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllProyectoGradosWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(proyectoGradoServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restProyectoGradoMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(proyectoGradoRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getProyectoGrado() throws Exception {
        // Initialize the database
        insertedProyectoGrado = proyectoGradoRepository.saveAndFlush(proyectoGrado);

        // Get the proyectoGrado
        restProyectoGradoMockMvc
            .perform(get(ENTITY_API_URL_ID, proyectoGrado.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(proyectoGrado.getId().intValue()))
            .andExpect(jsonPath("$.titulo").value(DEFAULT_TITULO))
            .andExpect(jsonPath("$.resumen").value(DEFAULT_RESUMEN))
            .andExpect(jsonPath("$.programa").value(DEFAULT_PROGRAMA))
            .andExpect(jsonPath("$.etapaActual").value(DEFAULT_ETAPA_ACTUAL.toString()))
            .andExpect(jsonPath("$.estado").value(DEFAULT_ESTADO.toString()))
            .andExpect(jsonPath("$.porcentajeAvance").value(DEFAULT_PORCENTAJE_AVANCE))
            .andExpect(jsonPath("$.fechaRegistro").value(DEFAULT_FECHA_REGISTRO.toString()))
            .andExpect(jsonPath("$.fechaActualizacion").value(DEFAULT_FECHA_ACTUALIZACION.toString()));
    }

    @Test
    @Transactional
    void getProyectoGradosByIdFiltering() throws Exception {
        // Initialize the database
        insertedProyectoGrado = proyectoGradoRepository.saveAndFlush(proyectoGrado);

        Long id = proyectoGrado.getId();

        defaultProyectoGradoFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultProyectoGradoFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultProyectoGradoFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllProyectoGradosByTituloIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedProyectoGrado = proyectoGradoRepository.saveAndFlush(proyectoGrado);

        // Get all the proyectoGradoList where titulo equals to
        defaultProyectoGradoFiltering("titulo.equals=" + DEFAULT_TITULO, "titulo.equals=" + UPDATED_TITULO);
    }

    @Test
    @Transactional
    void getAllProyectoGradosByTituloIsInShouldWork() throws Exception {
        // Initialize the database
        insertedProyectoGrado = proyectoGradoRepository.saveAndFlush(proyectoGrado);

        // Get all the proyectoGradoList where titulo in
        defaultProyectoGradoFiltering("titulo.in=" + DEFAULT_TITULO + "," + UPDATED_TITULO, "titulo.in=" + UPDATED_TITULO);
    }

    @Test
    @Transactional
    void getAllProyectoGradosByTituloIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedProyectoGrado = proyectoGradoRepository.saveAndFlush(proyectoGrado);

        // Get all the proyectoGradoList where titulo is not null
        defaultProyectoGradoFiltering("titulo.specified=true", "titulo.specified=false");
    }

    @Test
    @Transactional
    void getAllProyectoGradosByTituloContainsSomething() throws Exception {
        // Initialize the database
        insertedProyectoGrado = proyectoGradoRepository.saveAndFlush(proyectoGrado);

        // Get all the proyectoGradoList where titulo contains
        defaultProyectoGradoFiltering("titulo.contains=" + DEFAULT_TITULO, "titulo.contains=" + UPDATED_TITULO);
    }

    @Test
    @Transactional
    void getAllProyectoGradosByTituloNotContainsSomething() throws Exception {
        // Initialize the database
        insertedProyectoGrado = proyectoGradoRepository.saveAndFlush(proyectoGrado);

        // Get all the proyectoGradoList where titulo does not contain
        defaultProyectoGradoFiltering("titulo.doesNotContain=" + UPDATED_TITULO, "titulo.doesNotContain=" + DEFAULT_TITULO);
    }

    @Test
    @Transactional
    void getAllProyectoGradosByProgramaIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedProyectoGrado = proyectoGradoRepository.saveAndFlush(proyectoGrado);

        // Get all the proyectoGradoList where programa equals to
        defaultProyectoGradoFiltering("programa.equals=" + DEFAULT_PROGRAMA, "programa.equals=" + UPDATED_PROGRAMA);
    }

    @Test
    @Transactional
    void getAllProyectoGradosByProgramaIsInShouldWork() throws Exception {
        // Initialize the database
        insertedProyectoGrado = proyectoGradoRepository.saveAndFlush(proyectoGrado);

        // Get all the proyectoGradoList where programa in
        defaultProyectoGradoFiltering("programa.in=" + DEFAULT_PROGRAMA + "," + UPDATED_PROGRAMA, "programa.in=" + UPDATED_PROGRAMA);
    }

    @Test
    @Transactional
    void getAllProyectoGradosByProgramaIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedProyectoGrado = proyectoGradoRepository.saveAndFlush(proyectoGrado);

        // Get all the proyectoGradoList where programa is not null
        defaultProyectoGradoFiltering("programa.specified=true", "programa.specified=false");
    }

    @Test
    @Transactional
    void getAllProyectoGradosByProgramaContainsSomething() throws Exception {
        // Initialize the database
        insertedProyectoGrado = proyectoGradoRepository.saveAndFlush(proyectoGrado);

        // Get all the proyectoGradoList where programa contains
        defaultProyectoGradoFiltering("programa.contains=" + DEFAULT_PROGRAMA, "programa.contains=" + UPDATED_PROGRAMA);
    }

    @Test
    @Transactional
    void getAllProyectoGradosByProgramaNotContainsSomething() throws Exception {
        // Initialize the database
        insertedProyectoGrado = proyectoGradoRepository.saveAndFlush(proyectoGrado);

        // Get all the proyectoGradoList where programa does not contain
        defaultProyectoGradoFiltering("programa.doesNotContain=" + UPDATED_PROGRAMA, "programa.doesNotContain=" + DEFAULT_PROGRAMA);
    }

    @Test
    @Transactional
    void getAllProyectoGradosByEtapaActualIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedProyectoGrado = proyectoGradoRepository.saveAndFlush(proyectoGrado);

        // Get all the proyectoGradoList where etapaActual equals to
        defaultProyectoGradoFiltering("etapaActual.equals=" + DEFAULT_ETAPA_ACTUAL, "etapaActual.equals=" + UPDATED_ETAPA_ACTUAL);
    }

    @Test
    @Transactional
    void getAllProyectoGradosByEtapaActualIsInShouldWork() throws Exception {
        // Initialize the database
        insertedProyectoGrado = proyectoGradoRepository.saveAndFlush(proyectoGrado);

        // Get all the proyectoGradoList where etapaActual in
        defaultProyectoGradoFiltering(
            "etapaActual.in=" + DEFAULT_ETAPA_ACTUAL + "," + UPDATED_ETAPA_ACTUAL,
            "etapaActual.in=" + UPDATED_ETAPA_ACTUAL
        );
    }

    @Test
    @Transactional
    void getAllProyectoGradosByEtapaActualIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedProyectoGrado = proyectoGradoRepository.saveAndFlush(proyectoGrado);

        // Get all the proyectoGradoList where etapaActual is not null
        defaultProyectoGradoFiltering("etapaActual.specified=true", "etapaActual.specified=false");
    }

    @Test
    @Transactional
    void getAllProyectoGradosByEstadoIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedProyectoGrado = proyectoGradoRepository.saveAndFlush(proyectoGrado);

        // Get all the proyectoGradoList where estado equals to
        defaultProyectoGradoFiltering("estado.equals=" + DEFAULT_ESTADO, "estado.equals=" + UPDATED_ESTADO);
    }

    @Test
    @Transactional
    void getAllProyectoGradosByEstadoIsInShouldWork() throws Exception {
        // Initialize the database
        insertedProyectoGrado = proyectoGradoRepository.saveAndFlush(proyectoGrado);

        // Get all the proyectoGradoList where estado in
        defaultProyectoGradoFiltering("estado.in=" + DEFAULT_ESTADO + "," + UPDATED_ESTADO, "estado.in=" + UPDATED_ESTADO);
    }

    @Test
    @Transactional
    void getAllProyectoGradosByEstadoIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedProyectoGrado = proyectoGradoRepository.saveAndFlush(proyectoGrado);

        // Get all the proyectoGradoList where estado is not null
        defaultProyectoGradoFiltering("estado.specified=true", "estado.specified=false");
    }

    @Test
    @Transactional
    void getAllProyectoGradosByPorcentajeAvanceIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedProyectoGrado = proyectoGradoRepository.saveAndFlush(proyectoGrado);

        // Get all the proyectoGradoList where porcentajeAvance equals to
        defaultProyectoGradoFiltering(
            "porcentajeAvance.equals=" + DEFAULT_PORCENTAJE_AVANCE,
            "porcentajeAvance.equals=" + UPDATED_PORCENTAJE_AVANCE
        );
    }

    @Test
    @Transactional
    void getAllProyectoGradosByPorcentajeAvanceIsInShouldWork() throws Exception {
        // Initialize the database
        insertedProyectoGrado = proyectoGradoRepository.saveAndFlush(proyectoGrado);

        // Get all the proyectoGradoList where porcentajeAvance in
        defaultProyectoGradoFiltering(
            "porcentajeAvance.in=" + DEFAULT_PORCENTAJE_AVANCE + "," + UPDATED_PORCENTAJE_AVANCE,
            "porcentajeAvance.in=" + UPDATED_PORCENTAJE_AVANCE
        );
    }

    @Test
    @Transactional
    void getAllProyectoGradosByPorcentajeAvanceIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedProyectoGrado = proyectoGradoRepository.saveAndFlush(proyectoGrado);

        // Get all the proyectoGradoList where porcentajeAvance is not null
        defaultProyectoGradoFiltering("porcentajeAvance.specified=true", "porcentajeAvance.specified=false");
    }

    @Test
    @Transactional
    void getAllProyectoGradosByPorcentajeAvanceIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedProyectoGrado = proyectoGradoRepository.saveAndFlush(proyectoGrado);

        // Get all the proyectoGradoList where porcentajeAvance is greater than or equal to
        defaultProyectoGradoFiltering(
            "porcentajeAvance.greaterThanOrEqual=" + DEFAULT_PORCENTAJE_AVANCE,
            "porcentajeAvance.greaterThanOrEqual=" + (DEFAULT_PORCENTAJE_AVANCE + 1)
        );
    }

    @Test
    @Transactional
    void getAllProyectoGradosByPorcentajeAvanceIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedProyectoGrado = proyectoGradoRepository.saveAndFlush(proyectoGrado);

        // Get all the proyectoGradoList where porcentajeAvance is less than or equal to
        defaultProyectoGradoFiltering(
            "porcentajeAvance.lessThanOrEqual=" + DEFAULT_PORCENTAJE_AVANCE,
            "porcentajeAvance.lessThanOrEqual=" + SMALLER_PORCENTAJE_AVANCE
        );
    }

    @Test
    @Transactional
    void getAllProyectoGradosByPorcentajeAvanceIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedProyectoGrado = proyectoGradoRepository.saveAndFlush(proyectoGrado);

        // Get all the proyectoGradoList where porcentajeAvance is less than
        defaultProyectoGradoFiltering(
            "porcentajeAvance.lessThan=" + (DEFAULT_PORCENTAJE_AVANCE + 1),
            "porcentajeAvance.lessThan=" + DEFAULT_PORCENTAJE_AVANCE
        );
    }

    @Test
    @Transactional
    void getAllProyectoGradosByPorcentajeAvanceIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedProyectoGrado = proyectoGradoRepository.saveAndFlush(proyectoGrado);

        // Get all the proyectoGradoList where porcentajeAvance is greater than
        defaultProyectoGradoFiltering(
            "porcentajeAvance.greaterThan=" + SMALLER_PORCENTAJE_AVANCE,
            "porcentajeAvance.greaterThan=" + DEFAULT_PORCENTAJE_AVANCE
        );
    }

    @Test
    @Transactional
    void getAllProyectoGradosByFechaRegistroIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedProyectoGrado = proyectoGradoRepository.saveAndFlush(proyectoGrado);

        // Get all the proyectoGradoList where fechaRegistro equals to
        defaultProyectoGradoFiltering("fechaRegistro.equals=" + DEFAULT_FECHA_REGISTRO, "fechaRegistro.equals=" + UPDATED_FECHA_REGISTRO);
    }

    @Test
    @Transactional
    void getAllProyectoGradosByFechaRegistroIsInShouldWork() throws Exception {
        // Initialize the database
        insertedProyectoGrado = proyectoGradoRepository.saveAndFlush(proyectoGrado);

        // Get all the proyectoGradoList where fechaRegistro in
        defaultProyectoGradoFiltering(
            "fechaRegistro.in=" + DEFAULT_FECHA_REGISTRO + "," + UPDATED_FECHA_REGISTRO,
            "fechaRegistro.in=" + UPDATED_FECHA_REGISTRO
        );
    }

    @Test
    @Transactional
    void getAllProyectoGradosByFechaRegistroIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedProyectoGrado = proyectoGradoRepository.saveAndFlush(proyectoGrado);

        // Get all the proyectoGradoList where fechaRegistro is not null
        defaultProyectoGradoFiltering("fechaRegistro.specified=true", "fechaRegistro.specified=false");
    }

    @Test
    @Transactional
    void getAllProyectoGradosByFechaActualizacionIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedProyectoGrado = proyectoGradoRepository.saveAndFlush(proyectoGrado);

        // Get all the proyectoGradoList where fechaActualizacion equals to
        defaultProyectoGradoFiltering(
            "fechaActualizacion.equals=" + DEFAULT_FECHA_ACTUALIZACION,
            "fechaActualizacion.equals=" + UPDATED_FECHA_ACTUALIZACION
        );
    }

    @Test
    @Transactional
    void getAllProyectoGradosByFechaActualizacionIsInShouldWork() throws Exception {
        // Initialize the database
        insertedProyectoGrado = proyectoGradoRepository.saveAndFlush(proyectoGrado);

        // Get all the proyectoGradoList where fechaActualizacion in
        defaultProyectoGradoFiltering(
            "fechaActualizacion.in=" + DEFAULT_FECHA_ACTUALIZACION + "," + UPDATED_FECHA_ACTUALIZACION,
            "fechaActualizacion.in=" + UPDATED_FECHA_ACTUALIZACION
        );
    }

    @Test
    @Transactional
    void getAllProyectoGradosByFechaActualizacionIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedProyectoGrado = proyectoGradoRepository.saveAndFlush(proyectoGrado);

        // Get all the proyectoGradoList where fechaActualizacion is not null
        defaultProyectoGradoFiltering("fechaActualizacion.specified=true", "fechaActualizacion.specified=false");
    }

    @Test
    @Transactional
    void getAllProyectoGradosByTutorIsEqualToSomething() throws Exception {
        User tutor;
        if (TestUtil.findAll(em, User.class).isEmpty()) {
            proyectoGradoRepository.saveAndFlush(proyectoGrado);
            tutor = UserResourceIT.createEntity();
        } else {
            tutor = TestUtil.findAll(em, User.class).getFirst();
        }
        em.persist(tutor);
        em.flush();
        proyectoGrado.setTutor(tutor);
        proyectoGradoRepository.saveAndFlush(proyectoGrado);
        Long tutorId = tutor.getId();
        // Get all the proyectoGradoList where tutor equals to tutorId
        defaultProyectoGradoShouldBeFound("tutorId.equals=" + tutorId);

        // Get all the proyectoGradoList where tutor equals to (tutorId + 1)
        defaultProyectoGradoShouldNotBeFound("tutorId.equals=" + (tutorId + 1));
    }

    @Test
    @Transactional
    void getAllProyectoGradosByEstudiantesIsEqualToSomething() throws Exception {
        User estudiantes;
        if (TestUtil.findAll(em, User.class).isEmpty()) {
            proyectoGradoRepository.saveAndFlush(proyectoGrado);
            estudiantes = UserResourceIT.createEntity();
        } else {
            estudiantes = TestUtil.findAll(em, User.class).getFirst();
        }
        em.persist(estudiantes);
        em.flush();
        proyectoGrado.addEstudiantes(estudiantes);
        proyectoGradoRepository.saveAndFlush(proyectoGrado);
        Long estudiantesId = estudiantes.getId();
        // Get all the proyectoGradoList where estudiantes equals to estudiantesId
        defaultProyectoGradoShouldBeFound("estudiantesId.equals=" + estudiantesId);

        // Get all the proyectoGradoList where estudiantes equals to (estudiantesId + 1)
        defaultProyectoGradoShouldNotBeFound("estudiantesId.equals=" + (estudiantesId + 1));
    }

    @Test
    @Transactional
    void getAllProyectoGradosByJuradosIsEqualToSomething() throws Exception {
        User jurados;
        if (TestUtil.findAll(em, User.class).isEmpty()) {
            proyectoGradoRepository.saveAndFlush(proyectoGrado);
            jurados = UserResourceIT.createEntity();
        } else {
            jurados = TestUtil.findAll(em, User.class).getFirst();
        }
        em.persist(jurados);
        em.flush();
        proyectoGrado.addJurados(jurados);
        proyectoGradoRepository.saveAndFlush(proyectoGrado);
        Long juradosId = jurados.getId();
        // Get all the proyectoGradoList where jurados equals to juradosId
        defaultProyectoGradoShouldBeFound("juradosId.equals=" + juradosId);

        // Get all the proyectoGradoList where jurados equals to (juradosId + 1)
        defaultProyectoGradoShouldNotBeFound("juradosId.equals=" + (juradosId + 1));
    }

    private void defaultProyectoGradoFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultProyectoGradoShouldBeFound(shouldBeFound);
        defaultProyectoGradoShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultProyectoGradoShouldBeFound(String filter) throws Exception {
        restProyectoGradoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(proyectoGrado.getId().intValue())))
            .andExpect(jsonPath("$.[*].titulo").value(hasItem(DEFAULT_TITULO)))
            .andExpect(jsonPath("$.[*].resumen").value(hasItem(DEFAULT_RESUMEN)))
            .andExpect(jsonPath("$.[*].programa").value(hasItem(DEFAULT_PROGRAMA)))
            .andExpect(jsonPath("$.[*].etapaActual").value(hasItem(DEFAULT_ETAPA_ACTUAL.toString())))
            .andExpect(jsonPath("$.[*].estado").value(hasItem(DEFAULT_ESTADO.toString())))
            .andExpect(jsonPath("$.[*].porcentajeAvance").value(hasItem(DEFAULT_PORCENTAJE_AVANCE)))
            .andExpect(jsonPath("$.[*].fechaRegistro").value(hasItem(DEFAULT_FECHA_REGISTRO.toString())))
            .andExpect(jsonPath("$.[*].fechaActualizacion").value(hasItem(DEFAULT_FECHA_ACTUALIZACION.toString())));

        // Check, that the count call also returns 1
        restProyectoGradoMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultProyectoGradoShouldNotBeFound(String filter) throws Exception {
        restProyectoGradoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restProyectoGradoMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingProyectoGrado() throws Exception {
        // Get the proyectoGrado
        restProyectoGradoMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingProyectoGrado() throws Exception {
        // Initialize the database
        insertedProyectoGrado = proyectoGradoRepository.saveAndFlush(proyectoGrado);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the proyectoGrado
        ProyectoGrado updatedProyectoGrado = proyectoGradoRepository.findById(proyectoGrado.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedProyectoGrado are not directly saved in db
        em.detach(updatedProyectoGrado);
        updatedProyectoGrado
            .titulo(UPDATED_TITULO)
            .resumen(UPDATED_RESUMEN)
            .programa(UPDATED_PROGRAMA)
            .etapaActual(UPDATED_ETAPA_ACTUAL)
            .estado(UPDATED_ESTADO)
            .porcentajeAvance(UPDATED_PORCENTAJE_AVANCE)
            .fechaRegistro(UPDATED_FECHA_REGISTRO)
            .fechaActualizacion(UPDATED_FECHA_ACTUALIZACION);
        ProyectoGradoDTO proyectoGradoDTO = proyectoGradoMapper.toDto(updatedProyectoGrado);

        restProyectoGradoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, proyectoGradoDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(proyectoGradoDTO))
            )
            .andExpect(status().isOk());

        // Validate the ProyectoGrado in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedProyectoGradoToMatchAllProperties(updatedProyectoGrado);
    }

    @Test
    @Transactional
    void putNonExistingProyectoGrado() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        proyectoGrado.setId(longCount.incrementAndGet());

        // Create the ProyectoGrado
        ProyectoGradoDTO proyectoGradoDTO = proyectoGradoMapper.toDto(proyectoGrado);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProyectoGradoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, proyectoGradoDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(proyectoGradoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ProyectoGrado in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchProyectoGrado() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        proyectoGrado.setId(longCount.incrementAndGet());

        // Create the ProyectoGrado
        ProyectoGradoDTO proyectoGradoDTO = proyectoGradoMapper.toDto(proyectoGrado);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProyectoGradoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(proyectoGradoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ProyectoGrado in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamProyectoGrado() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        proyectoGrado.setId(longCount.incrementAndGet());

        // Create the ProyectoGrado
        ProyectoGradoDTO proyectoGradoDTO = proyectoGradoMapper.toDto(proyectoGrado);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProyectoGradoMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(proyectoGradoDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ProyectoGrado in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateProyectoGradoWithPatch() throws Exception {
        // Initialize the database
        insertedProyectoGrado = proyectoGradoRepository.saveAndFlush(proyectoGrado);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the proyectoGrado using partial update
        ProyectoGrado partialUpdatedProyectoGrado = new ProyectoGrado();
        partialUpdatedProyectoGrado.setId(proyectoGrado.getId());

        partialUpdatedProyectoGrado
            .titulo(UPDATED_TITULO)
            .porcentajeAvance(UPDATED_PORCENTAJE_AVANCE)
            .fechaRegistro(UPDATED_FECHA_REGISTRO);

        restProyectoGradoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedProyectoGrado.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedProyectoGrado))
            )
            .andExpect(status().isOk());

        // Validate the ProyectoGrado in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertProyectoGradoUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedProyectoGrado, proyectoGrado),
            getPersistedProyectoGrado(proyectoGrado)
        );
    }

    @Test
    @Transactional
    void fullUpdateProyectoGradoWithPatch() throws Exception {
        // Initialize the database
        insertedProyectoGrado = proyectoGradoRepository.saveAndFlush(proyectoGrado);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the proyectoGrado using partial update
        ProyectoGrado partialUpdatedProyectoGrado = new ProyectoGrado();
        partialUpdatedProyectoGrado.setId(proyectoGrado.getId());

        partialUpdatedProyectoGrado
            .titulo(UPDATED_TITULO)
            .resumen(UPDATED_RESUMEN)
            .programa(UPDATED_PROGRAMA)
            .etapaActual(UPDATED_ETAPA_ACTUAL)
            .estado(UPDATED_ESTADO)
            .porcentajeAvance(UPDATED_PORCENTAJE_AVANCE)
            .fechaRegistro(UPDATED_FECHA_REGISTRO)
            .fechaActualizacion(UPDATED_FECHA_ACTUALIZACION);

        restProyectoGradoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedProyectoGrado.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedProyectoGrado))
            )
            .andExpect(status().isOk());

        // Validate the ProyectoGrado in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertProyectoGradoUpdatableFieldsEquals(partialUpdatedProyectoGrado, getPersistedProyectoGrado(partialUpdatedProyectoGrado));
    }

    @Test
    @Transactional
    void patchNonExistingProyectoGrado() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        proyectoGrado.setId(longCount.incrementAndGet());

        // Create the ProyectoGrado
        ProyectoGradoDTO proyectoGradoDTO = proyectoGradoMapper.toDto(proyectoGrado);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProyectoGradoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, proyectoGradoDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(proyectoGradoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ProyectoGrado in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchProyectoGrado() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        proyectoGrado.setId(longCount.incrementAndGet());

        // Create the ProyectoGrado
        ProyectoGradoDTO proyectoGradoDTO = proyectoGradoMapper.toDto(proyectoGrado);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProyectoGradoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(proyectoGradoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ProyectoGrado in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamProyectoGrado() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        proyectoGrado.setId(longCount.incrementAndGet());

        // Create the ProyectoGrado
        ProyectoGradoDTO proyectoGradoDTO = proyectoGradoMapper.toDto(proyectoGrado);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProyectoGradoMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(proyectoGradoDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ProyectoGrado in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteProyectoGrado() throws Exception {
        // Initialize the database
        insertedProyectoGrado = proyectoGradoRepository.saveAndFlush(proyectoGrado);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the proyectoGrado
        restProyectoGradoMockMvc
            .perform(delete(ENTITY_API_URL_ID, proyectoGrado.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return proyectoGradoRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected ProyectoGrado getPersistedProyectoGrado(ProyectoGrado proyectoGrado) {
        return proyectoGradoRepository.findById(proyectoGrado.getId()).orElseThrow();
    }

    protected void assertPersistedProyectoGradoToMatchAllProperties(ProyectoGrado expectedProyectoGrado) {
        assertProyectoGradoAllPropertiesEquals(expectedProyectoGrado, getPersistedProyectoGrado(expectedProyectoGrado));
    }

    protected void assertPersistedProyectoGradoToMatchUpdatableProperties(ProyectoGrado expectedProyectoGrado) {
        assertProyectoGradoAllUpdatablePropertiesEquals(expectedProyectoGrado, getPersistedProyectoGrado(expectedProyectoGrado));
    }
}
