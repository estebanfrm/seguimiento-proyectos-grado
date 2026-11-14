package co.edu.sgpg.web.rest;

import static co.edu.sgpg.domain.EntregaAsserts.*;
import static co.edu.sgpg.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import co.edu.sgpg.IntegrationTest;
import co.edu.sgpg.domain.Entrega;
import co.edu.sgpg.domain.ProyectoGrado;
import co.edu.sgpg.domain.User;
import co.edu.sgpg.domain.enumeration.EstadoEntrega;
import co.edu.sgpg.domain.enumeration.Etapa;
import co.edu.sgpg.repository.EntregaRepository;
import co.edu.sgpg.repository.UserRepository;
import co.edu.sgpg.service.EntregaService;
import co.edu.sgpg.service.dto.EntregaDTO;
import co.edu.sgpg.service.mapper.EntregaMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
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
 * Integration tests for the {@link EntregaResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class EntregaResourceIT {

    private static final Etapa DEFAULT_ETAPA = Etapa.PROPUESTA;
    private static final Etapa UPDATED_ETAPA = Etapa.ANTEPROYECTO;

    private static final String DEFAULT_TITULO = "AAAAAAAAAA";
    private static final String UPDATED_TITULO = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPCION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPCION = "BBBBBBBBBB";

    private static final byte[] DEFAULT_ARCHIVO = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_ARCHIVO = TestUtil.createByteArray(1, "1");
    private static final String DEFAULT_ARCHIVO_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_ARCHIVO_CONTENT_TYPE = "image/png";

    private static final String DEFAULT_URL_DOCUMENTO = "AAAAAAAAAA";
    private static final String UPDATED_URL_DOCUMENTO = "BBBBBBBBBB";

    private static final Integer DEFAULT_VERSION = 1;
    private static final Integer UPDATED_VERSION = 2;
    private static final Integer SMALLER_VERSION = 1 - 1;

    private static final Instant DEFAULT_FECHA_ENTREGA = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_FECHA_ENTREGA = Instant.ofEpochMilli(1701983268847L);

    private static final EstadoEntrega DEFAULT_ESTADO = EstadoEntrega.ENVIADA;
    private static final EstadoEntrega UPDATED_ESTADO = EstadoEntrega.EN_REVISION;

    private static final String ENTITY_API_URL = "/api/entregas";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private EntregaRepository entregaRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private EntregaRepository entregaRepositoryMock;

    @Autowired
    private EntregaMapper entregaMapper;

    @Mock
    private EntregaService entregaServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restEntregaMockMvc;

    private Entrega entrega;

    private Entrega insertedEntrega;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Entrega createEntity(EntityManager em) {
        Entrega entrega = new Entrega()
            .etapa(DEFAULT_ETAPA)
            .titulo(DEFAULT_TITULO)
            .descripcion(DEFAULT_DESCRIPCION)
            .archivo(DEFAULT_ARCHIVO)
            .archivoContentType(DEFAULT_ARCHIVO_CONTENT_TYPE)
            .urlDocumento(DEFAULT_URL_DOCUMENTO)
            .version(DEFAULT_VERSION)
            .fechaEntrega(DEFAULT_FECHA_ENTREGA)
            .estado(DEFAULT_ESTADO);
        // Add required entity
        ProyectoGrado proyectoGrado;
        if (TestUtil.findAll(em, ProyectoGrado.class).isEmpty()) {
            proyectoGrado = ProyectoGradoResourceIT.createEntity();
            em.persist(proyectoGrado);
            em.flush();
        } else {
            proyectoGrado = TestUtil.findAll(em, ProyectoGrado.class).getFirst();
        }
        entrega.setProyecto(proyectoGrado);
        return entrega;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Entrega createUpdatedEntity(EntityManager em) {
        Entrega updatedEntrega = new Entrega()
            .etapa(UPDATED_ETAPA)
            .titulo(UPDATED_TITULO)
            .descripcion(UPDATED_DESCRIPCION)
            .archivo(UPDATED_ARCHIVO)
            .archivoContentType(UPDATED_ARCHIVO_CONTENT_TYPE)
            .urlDocumento(UPDATED_URL_DOCUMENTO)
            .version(UPDATED_VERSION)
            .fechaEntrega(UPDATED_FECHA_ENTREGA)
            .estado(UPDATED_ESTADO);
        // Add required entity
        ProyectoGrado proyectoGrado;
        if (TestUtil.findAll(em, ProyectoGrado.class).isEmpty()) {
            proyectoGrado = ProyectoGradoResourceIT.createUpdatedEntity();
            em.persist(proyectoGrado);
            em.flush();
        } else {
            proyectoGrado = TestUtil.findAll(em, ProyectoGrado.class).getFirst();
        }
        updatedEntrega.setProyecto(proyectoGrado);
        return updatedEntrega;
    }

    @BeforeEach
    void initTest() {
        entrega = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedEntrega != null) {
            entregaRepository.delete(insertedEntrega);
            insertedEntrega = null;
        }
    }

    @Test
    @Transactional
    void createEntrega() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Entrega
        EntregaDTO entregaDTO = entregaMapper.toDto(entrega);
        var returnedEntregaDTO = om.readValue(
            restEntregaMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(entregaDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            EntregaDTO.class
        );

        // Validate the Entrega in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedEntrega = entregaMapper.toEntity(returnedEntregaDTO);
        assertEntregaUpdatableFieldsEquals(returnedEntrega, getPersistedEntrega(returnedEntrega));

        insertedEntrega = returnedEntrega;
    }

    @Test
    @Transactional
    void createEntregaWithExistingId() throws Exception {
        // Create the Entrega with an existing ID
        entrega.setId(1L);
        EntregaDTO entregaDTO = entregaMapper.toDto(entrega);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restEntregaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(entregaDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Entrega in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkEtapaIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        entrega.setEtapa(null);

        // Create the Entrega, which fails.
        EntregaDTO entregaDTO = entregaMapper.toDto(entrega);

        restEntregaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(entregaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTituloIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        entrega.setTitulo(null);

        // Create the Entrega, which fails.
        EntregaDTO entregaDTO = entregaMapper.toDto(entrega);

        restEntregaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(entregaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkVersionIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        entrega.setVersion(null);

        // Create the Entrega, which fails.
        EntregaDTO entregaDTO = entregaMapper.toDto(entrega);

        restEntregaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(entregaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkFechaEntregaIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        entrega.setFechaEntrega(null);

        // Create the Entrega, which fails.
        EntregaDTO entregaDTO = entregaMapper.toDto(entrega);

        restEntregaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(entregaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkEstadoIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        entrega.setEstado(null);

        // Create the Entrega, which fails.
        EntregaDTO entregaDTO = entregaMapper.toDto(entrega);

        restEntregaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(entregaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllEntregas() throws Exception {
        // Initialize the database
        insertedEntrega = entregaRepository.saveAndFlush(entrega);

        // Get all the entregaList
        restEntregaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(entrega.getId().intValue())))
            .andExpect(jsonPath("$.[*].etapa").value(hasItem(DEFAULT_ETAPA.toString())))
            .andExpect(jsonPath("$.[*].titulo").value(hasItem(DEFAULT_TITULO)))
            .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION)))
            .andExpect(jsonPath("$.[*].archivoContentType").value(hasItem(DEFAULT_ARCHIVO_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].archivo").value(hasItem(Base64.getEncoder().encodeToString(DEFAULT_ARCHIVO))))
            .andExpect(jsonPath("$.[*].urlDocumento").value(hasItem(DEFAULT_URL_DOCUMENTO)))
            .andExpect(jsonPath("$.[*].version").value(hasItem(DEFAULT_VERSION)))
            .andExpect(jsonPath("$.[*].fechaEntrega").value(hasItem(DEFAULT_FECHA_ENTREGA.toString())))
            .andExpect(jsonPath("$.[*].estado").value(hasItem(DEFAULT_ESTADO.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllEntregasWithEagerRelationshipsIsEnabled() throws Exception {
        when(entregaServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restEntregaMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(entregaServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllEntregasWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(entregaServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restEntregaMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(entregaRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getEntrega() throws Exception {
        // Initialize the database
        insertedEntrega = entregaRepository.saveAndFlush(entrega);

        // Get the entrega
        restEntregaMockMvc
            .perform(get(ENTITY_API_URL_ID, entrega.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(entrega.getId().intValue()))
            .andExpect(jsonPath("$.etapa").value(DEFAULT_ETAPA.toString()))
            .andExpect(jsonPath("$.titulo").value(DEFAULT_TITULO))
            .andExpect(jsonPath("$.descripcion").value(DEFAULT_DESCRIPCION))
            .andExpect(jsonPath("$.archivoContentType").value(DEFAULT_ARCHIVO_CONTENT_TYPE))
            .andExpect(jsonPath("$.archivo").value(Base64.getEncoder().encodeToString(DEFAULT_ARCHIVO)))
            .andExpect(jsonPath("$.urlDocumento").value(DEFAULT_URL_DOCUMENTO))
            .andExpect(jsonPath("$.version").value(DEFAULT_VERSION))
            .andExpect(jsonPath("$.fechaEntrega").value(DEFAULT_FECHA_ENTREGA.toString()))
            .andExpect(jsonPath("$.estado").value(DEFAULT_ESTADO.toString()));
    }

    @Test
    @Transactional
    void getEntregasByIdFiltering() throws Exception {
        // Initialize the database
        insertedEntrega = entregaRepository.saveAndFlush(entrega);

        Long id = entrega.getId();

        defaultEntregaFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultEntregaFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultEntregaFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllEntregasByEtapaIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedEntrega = entregaRepository.saveAndFlush(entrega);

        // Get all the entregaList where etapa equals to
        defaultEntregaFiltering("etapa.equals=" + DEFAULT_ETAPA, "etapa.equals=" + UPDATED_ETAPA);
    }

    @Test
    @Transactional
    void getAllEntregasByEtapaIsInShouldWork() throws Exception {
        // Initialize the database
        insertedEntrega = entregaRepository.saveAndFlush(entrega);

        // Get all the entregaList where etapa in
        defaultEntregaFiltering("etapa.in=" + DEFAULT_ETAPA + "," + UPDATED_ETAPA, "etapa.in=" + UPDATED_ETAPA);
    }

    @Test
    @Transactional
    void getAllEntregasByEtapaIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedEntrega = entregaRepository.saveAndFlush(entrega);

        // Get all the entregaList where etapa is not null
        defaultEntregaFiltering("etapa.specified=true", "etapa.specified=false");
    }

    @Test
    @Transactional
    void getAllEntregasByTituloIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedEntrega = entregaRepository.saveAndFlush(entrega);

        // Get all the entregaList where titulo equals to
        defaultEntregaFiltering("titulo.equals=" + DEFAULT_TITULO, "titulo.equals=" + UPDATED_TITULO);
    }

    @Test
    @Transactional
    void getAllEntregasByTituloIsInShouldWork() throws Exception {
        // Initialize the database
        insertedEntrega = entregaRepository.saveAndFlush(entrega);

        // Get all the entregaList where titulo in
        defaultEntregaFiltering("titulo.in=" + DEFAULT_TITULO + "," + UPDATED_TITULO, "titulo.in=" + UPDATED_TITULO);
    }

    @Test
    @Transactional
    void getAllEntregasByTituloIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedEntrega = entregaRepository.saveAndFlush(entrega);

        // Get all the entregaList where titulo is not null
        defaultEntregaFiltering("titulo.specified=true", "titulo.specified=false");
    }

    @Test
    @Transactional
    void getAllEntregasByTituloContainsSomething() throws Exception {
        // Initialize the database
        insertedEntrega = entregaRepository.saveAndFlush(entrega);

        // Get all the entregaList where titulo contains
        defaultEntregaFiltering("titulo.contains=" + DEFAULT_TITULO, "titulo.contains=" + UPDATED_TITULO);
    }

    @Test
    @Transactional
    void getAllEntregasByTituloNotContainsSomething() throws Exception {
        // Initialize the database
        insertedEntrega = entregaRepository.saveAndFlush(entrega);

        // Get all the entregaList where titulo does not contain
        defaultEntregaFiltering("titulo.doesNotContain=" + UPDATED_TITULO, "titulo.doesNotContain=" + DEFAULT_TITULO);
    }

    @Test
    @Transactional
    void getAllEntregasByUrlDocumentoIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedEntrega = entregaRepository.saveAndFlush(entrega);

        // Get all the entregaList where urlDocumento equals to
        defaultEntregaFiltering("urlDocumento.equals=" + DEFAULT_URL_DOCUMENTO, "urlDocumento.equals=" + UPDATED_URL_DOCUMENTO);
    }

    @Test
    @Transactional
    void getAllEntregasByUrlDocumentoIsInShouldWork() throws Exception {
        // Initialize the database
        insertedEntrega = entregaRepository.saveAndFlush(entrega);

        // Get all the entregaList where urlDocumento in
        defaultEntregaFiltering(
            "urlDocumento.in=" + DEFAULT_URL_DOCUMENTO + "," + UPDATED_URL_DOCUMENTO,
            "urlDocumento.in=" + UPDATED_URL_DOCUMENTO
        );
    }

    @Test
    @Transactional
    void getAllEntregasByUrlDocumentoIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedEntrega = entregaRepository.saveAndFlush(entrega);

        // Get all the entregaList where urlDocumento is not null
        defaultEntregaFiltering("urlDocumento.specified=true", "urlDocumento.specified=false");
    }

    @Test
    @Transactional
    void getAllEntregasByUrlDocumentoContainsSomething() throws Exception {
        // Initialize the database
        insertedEntrega = entregaRepository.saveAndFlush(entrega);

        // Get all the entregaList where urlDocumento contains
        defaultEntregaFiltering("urlDocumento.contains=" + DEFAULT_URL_DOCUMENTO, "urlDocumento.contains=" + UPDATED_URL_DOCUMENTO);
    }

    @Test
    @Transactional
    void getAllEntregasByUrlDocumentoNotContainsSomething() throws Exception {
        // Initialize the database
        insertedEntrega = entregaRepository.saveAndFlush(entrega);

        // Get all the entregaList where urlDocumento does not contain
        defaultEntregaFiltering(
            "urlDocumento.doesNotContain=" + UPDATED_URL_DOCUMENTO,
            "urlDocumento.doesNotContain=" + DEFAULT_URL_DOCUMENTO
        );
    }

    @Test
    @Transactional
    void getAllEntregasByVersionIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedEntrega = entregaRepository.saveAndFlush(entrega);

        // Get all the entregaList where version equals to
        defaultEntregaFiltering("version.equals=" + DEFAULT_VERSION, "version.equals=" + UPDATED_VERSION);
    }

    @Test
    @Transactional
    void getAllEntregasByVersionIsInShouldWork() throws Exception {
        // Initialize the database
        insertedEntrega = entregaRepository.saveAndFlush(entrega);

        // Get all the entregaList where version in
        defaultEntregaFiltering("version.in=" + DEFAULT_VERSION + "," + UPDATED_VERSION, "version.in=" + UPDATED_VERSION);
    }

    @Test
    @Transactional
    void getAllEntregasByVersionIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedEntrega = entregaRepository.saveAndFlush(entrega);

        // Get all the entregaList where version is not null
        defaultEntregaFiltering("version.specified=true", "version.specified=false");
    }

    @Test
    @Transactional
    void getAllEntregasByVersionIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedEntrega = entregaRepository.saveAndFlush(entrega);

        // Get all the entregaList where version is greater than or equal to
        defaultEntregaFiltering("version.greaterThanOrEqual=" + DEFAULT_VERSION, "version.greaterThanOrEqual=" + UPDATED_VERSION);
    }

    @Test
    @Transactional
    void getAllEntregasByVersionIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedEntrega = entregaRepository.saveAndFlush(entrega);

        // Get all the entregaList where version is less than or equal to
        defaultEntregaFiltering("version.lessThanOrEqual=" + DEFAULT_VERSION, "version.lessThanOrEqual=" + SMALLER_VERSION);
    }

    @Test
    @Transactional
    void getAllEntregasByVersionIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedEntrega = entregaRepository.saveAndFlush(entrega);

        // Get all the entregaList where version is less than
        defaultEntregaFiltering("version.lessThan=" + UPDATED_VERSION, "version.lessThan=" + DEFAULT_VERSION);
    }

    @Test
    @Transactional
    void getAllEntregasByVersionIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedEntrega = entregaRepository.saveAndFlush(entrega);

        // Get all the entregaList where version is greater than
        defaultEntregaFiltering("version.greaterThan=" + SMALLER_VERSION, "version.greaterThan=" + DEFAULT_VERSION);
    }

    @Test
    @Transactional
    void getAllEntregasByFechaEntregaIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedEntrega = entregaRepository.saveAndFlush(entrega);

        // Get all the entregaList where fechaEntrega equals to
        defaultEntregaFiltering("fechaEntrega.equals=" + DEFAULT_FECHA_ENTREGA, "fechaEntrega.equals=" + UPDATED_FECHA_ENTREGA);
    }

    @Test
    @Transactional
    void getAllEntregasByFechaEntregaIsInShouldWork() throws Exception {
        // Initialize the database
        insertedEntrega = entregaRepository.saveAndFlush(entrega);

        // Get all the entregaList where fechaEntrega in
        defaultEntregaFiltering(
            "fechaEntrega.in=" + DEFAULT_FECHA_ENTREGA + "," + UPDATED_FECHA_ENTREGA,
            "fechaEntrega.in=" + UPDATED_FECHA_ENTREGA
        );
    }

    @Test
    @Transactional
    void getAllEntregasByFechaEntregaIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedEntrega = entregaRepository.saveAndFlush(entrega);

        // Get all the entregaList where fechaEntrega is not null
        defaultEntregaFiltering("fechaEntrega.specified=true", "fechaEntrega.specified=false");
    }

    @Test
    @Transactional
    void getAllEntregasByEstadoIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedEntrega = entregaRepository.saveAndFlush(entrega);

        // Get all the entregaList where estado equals to
        defaultEntregaFiltering("estado.equals=" + DEFAULT_ESTADO, "estado.equals=" + UPDATED_ESTADO);
    }

    @Test
    @Transactional
    void getAllEntregasByEstadoIsInShouldWork() throws Exception {
        // Initialize the database
        insertedEntrega = entregaRepository.saveAndFlush(entrega);

        // Get all the entregaList where estado in
        defaultEntregaFiltering("estado.in=" + DEFAULT_ESTADO + "," + UPDATED_ESTADO, "estado.in=" + UPDATED_ESTADO);
    }

    @Test
    @Transactional
    void getAllEntregasByEstadoIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedEntrega = entregaRepository.saveAndFlush(entrega);

        // Get all the entregaList where estado is not null
        defaultEntregaFiltering("estado.specified=true", "estado.specified=false");
    }

    @Test
    @Transactional
    void getAllEntregasByAutorIsEqualToSomething() throws Exception {
        User autor;
        if (TestUtil.findAll(em, User.class).isEmpty()) {
            entregaRepository.saveAndFlush(entrega);
            autor = UserResourceIT.createEntity();
        } else {
            autor = TestUtil.findAll(em, User.class).getFirst();
        }
        em.persist(autor);
        em.flush();
        entrega.setAutor(autor);
        entregaRepository.saveAndFlush(entrega);
        Long autorId = autor.getId();
        // Get all the entregaList where autor equals to autorId
        defaultEntregaShouldBeFound("autorId.equals=" + autorId);

        // Get all the entregaList where autor equals to (autorId + 1)
        defaultEntregaShouldNotBeFound("autorId.equals=" + (autorId + 1));
    }

    @Test
    @Transactional
    void getAllEntregasByProyectoIsEqualToSomething() throws Exception {
        ProyectoGrado proyecto;
        if (TestUtil.findAll(em, ProyectoGrado.class).isEmpty()) {
            entregaRepository.saveAndFlush(entrega);
            proyecto = ProyectoGradoResourceIT.createEntity();
        } else {
            proyecto = TestUtil.findAll(em, ProyectoGrado.class).getFirst();
        }
        em.persist(proyecto);
        em.flush();
        entrega.setProyecto(proyecto);
        entregaRepository.saveAndFlush(entrega);
        Long proyectoId = proyecto.getId();
        // Get all the entregaList where proyecto equals to proyectoId
        defaultEntregaShouldBeFound("proyectoId.equals=" + proyectoId);

        // Get all the entregaList where proyecto equals to (proyectoId + 1)
        defaultEntregaShouldNotBeFound("proyectoId.equals=" + (proyectoId + 1));
    }

    private void defaultEntregaFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultEntregaShouldBeFound(shouldBeFound);
        defaultEntregaShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultEntregaShouldBeFound(String filter) throws Exception {
        restEntregaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(entrega.getId().intValue())))
            .andExpect(jsonPath("$.[*].etapa").value(hasItem(DEFAULT_ETAPA.toString())))
            .andExpect(jsonPath("$.[*].titulo").value(hasItem(DEFAULT_TITULO)))
            .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION)))
            .andExpect(jsonPath("$.[*].archivoContentType").value(hasItem(DEFAULT_ARCHIVO_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].archivo").value(hasItem(Base64.getEncoder().encodeToString(DEFAULT_ARCHIVO))))
            .andExpect(jsonPath("$.[*].urlDocumento").value(hasItem(DEFAULT_URL_DOCUMENTO)))
            .andExpect(jsonPath("$.[*].version").value(hasItem(DEFAULT_VERSION)))
            .andExpect(jsonPath("$.[*].fechaEntrega").value(hasItem(DEFAULT_FECHA_ENTREGA.toString())))
            .andExpect(jsonPath("$.[*].estado").value(hasItem(DEFAULT_ESTADO.toString())));

        // Check, that the count call also returns 1
        restEntregaMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultEntregaShouldNotBeFound(String filter) throws Exception {
        restEntregaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restEntregaMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingEntrega() throws Exception {
        // Get the entrega
        restEntregaMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingEntrega() throws Exception {
        // Initialize the database
        insertedEntrega = entregaRepository.saveAndFlush(entrega);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the entrega
        Entrega updatedEntrega = entregaRepository.findById(entrega.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedEntrega are not directly saved in db
        em.detach(updatedEntrega);
        updatedEntrega
            .etapa(UPDATED_ETAPA)
            .titulo(UPDATED_TITULO)
            .descripcion(UPDATED_DESCRIPCION)
            .archivo(UPDATED_ARCHIVO)
            .archivoContentType(UPDATED_ARCHIVO_CONTENT_TYPE)
            .urlDocumento(UPDATED_URL_DOCUMENTO)
            .version(UPDATED_VERSION)
            .fechaEntrega(UPDATED_FECHA_ENTREGA)
            .estado(UPDATED_ESTADO);
        EntregaDTO entregaDTO = entregaMapper.toDto(updatedEntrega);

        restEntregaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, entregaDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(entregaDTO))
            )
            .andExpect(status().isOk());

        // Validate the Entrega in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedEntregaToMatchAllProperties(updatedEntrega);
    }

    @Test
    @Transactional
    void putNonExistingEntrega() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        entrega.setId(longCount.incrementAndGet());

        // Create the Entrega
        EntregaDTO entregaDTO = entregaMapper.toDto(entrega);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restEntregaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, entregaDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(entregaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Entrega in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchEntrega() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        entrega.setId(longCount.incrementAndGet());

        // Create the Entrega
        EntregaDTO entregaDTO = entregaMapper.toDto(entrega);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEntregaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(entregaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Entrega in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamEntrega() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        entrega.setId(longCount.incrementAndGet());

        // Create the Entrega
        EntregaDTO entregaDTO = entregaMapper.toDto(entrega);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEntregaMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(entregaDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Entrega in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateEntregaWithPatch() throws Exception {
        // Initialize the database
        insertedEntrega = entregaRepository.saveAndFlush(entrega);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the entrega using partial update
        Entrega partialUpdatedEntrega = new Entrega();
        partialUpdatedEntrega.setId(entrega.getId());

        partialUpdatedEntrega
            .archivo(UPDATED_ARCHIVO)
            .archivoContentType(UPDATED_ARCHIVO_CONTENT_TYPE)
            .urlDocumento(UPDATED_URL_DOCUMENTO)
            .version(UPDATED_VERSION)
            .fechaEntrega(UPDATED_FECHA_ENTREGA)
            .estado(UPDATED_ESTADO);

        restEntregaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedEntrega.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedEntrega))
            )
            .andExpect(status().isOk());

        // Validate the Entrega in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertEntregaUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedEntrega, entrega), getPersistedEntrega(entrega));
    }

    @Test
    @Transactional
    void fullUpdateEntregaWithPatch() throws Exception {
        // Initialize the database
        insertedEntrega = entregaRepository.saveAndFlush(entrega);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the entrega using partial update
        Entrega partialUpdatedEntrega = new Entrega();
        partialUpdatedEntrega.setId(entrega.getId());

        partialUpdatedEntrega
            .etapa(UPDATED_ETAPA)
            .titulo(UPDATED_TITULO)
            .descripcion(UPDATED_DESCRIPCION)
            .archivo(UPDATED_ARCHIVO)
            .archivoContentType(UPDATED_ARCHIVO_CONTENT_TYPE)
            .urlDocumento(UPDATED_URL_DOCUMENTO)
            .version(UPDATED_VERSION)
            .fechaEntrega(UPDATED_FECHA_ENTREGA)
            .estado(UPDATED_ESTADO);

        restEntregaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedEntrega.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedEntrega))
            )
            .andExpect(status().isOk());

        // Validate the Entrega in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertEntregaUpdatableFieldsEquals(partialUpdatedEntrega, getPersistedEntrega(partialUpdatedEntrega));
    }

    @Test
    @Transactional
    void patchNonExistingEntrega() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        entrega.setId(longCount.incrementAndGet());

        // Create the Entrega
        EntregaDTO entregaDTO = entregaMapper.toDto(entrega);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restEntregaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, entregaDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(entregaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Entrega in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchEntrega() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        entrega.setId(longCount.incrementAndGet());

        // Create the Entrega
        EntregaDTO entregaDTO = entregaMapper.toDto(entrega);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEntregaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(entregaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Entrega in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamEntrega() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        entrega.setId(longCount.incrementAndGet());

        // Create the Entrega
        EntregaDTO entregaDTO = entregaMapper.toDto(entrega);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEntregaMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(entregaDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Entrega in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteEntrega() throws Exception {
        // Initialize the database
        insertedEntrega = entregaRepository.saveAndFlush(entrega);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the entrega
        restEntregaMockMvc
            .perform(delete(ENTITY_API_URL_ID, entrega.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return entregaRepository.count();
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

    protected Entrega getPersistedEntrega(Entrega entrega) {
        return entregaRepository.findById(entrega.getId()).orElseThrow();
    }

    protected void assertPersistedEntregaToMatchAllProperties(Entrega expectedEntrega) {
        assertEntregaAllPropertiesEquals(expectedEntrega, getPersistedEntrega(expectedEntrega));
    }

    protected void assertPersistedEntregaToMatchUpdatableProperties(Entrega expectedEntrega) {
        assertEntregaAllUpdatablePropertiesEquals(expectedEntrega, getPersistedEntrega(expectedEntrega));
    }
}
