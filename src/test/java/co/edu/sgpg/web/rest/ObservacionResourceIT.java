package co.edu.sgpg.web.rest;

import static co.edu.sgpg.domain.ObservacionAsserts.*;
import static co.edu.sgpg.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import co.edu.sgpg.IntegrationTest;
import co.edu.sgpg.domain.Entrega;
import co.edu.sgpg.domain.Observacion;
import co.edu.sgpg.domain.enumeration.TipoObservacion;
import co.edu.sgpg.repository.ObservacionRepository;
import co.edu.sgpg.repository.UserRepository;
import co.edu.sgpg.service.ObservacionService;
import co.edu.sgpg.service.dto.ObservacionDTO;
import co.edu.sgpg.service.mapper.ObservacionMapper;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/**
 * Integration tests for the {@link ObservacionResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser(username = "admin", authorities = co.edu.sgpg.security.AuthoritiesConstants.ADMIN)
class ObservacionResourceIT {

    private static final String DEFAULT_CONTENIDO = "AAAAAAAAAA";
    private static final String UPDATED_CONTENIDO = "BBBBBBBBBB";

    private static final TipoObservacion DEFAULT_TIPO = TipoObservacion.GENERAL;
    private static final TipoObservacion UPDATED_TIPO = TipoObservacion.CORRECCION_REQUERIDA;

    private static final Boolean DEFAULT_RESUELTA = false;
    private static final Boolean UPDATED_RESUELTA = true;

    private static final Instant DEFAULT_FECHA_CREACION = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_FECHA_CREACION = Instant.ofEpochMilli(1701983268847L);

    private static final String ENTITY_API_URL = "/api/observacions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ObservacionRepository observacionRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private ObservacionRepository observacionRepositoryMock;

    @Autowired
    private ObservacionMapper observacionMapper;

    @Mock
    private ObservacionService observacionServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restObservacionMockMvc;

    private Observacion observacion;

    private Observacion insertedObservacion;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Observacion createEntity(EntityManager em) {
        Observacion observacion = new Observacion()
            .contenido(DEFAULT_CONTENIDO)
            .tipo(DEFAULT_TIPO)
            .resuelta(DEFAULT_RESUELTA)
            .fechaCreacion(DEFAULT_FECHA_CREACION);
        // Add required entity
        Entrega entrega;
        if (TestUtil.findAll(em, Entrega.class).isEmpty()) {
            entrega = EntregaResourceIT.createEntity(em);
            em.persist(entrega);
            em.flush();
        } else {
            entrega = TestUtil.findAll(em, Entrega.class).getFirst();
        }
        observacion.setEntrega(entrega);
        return observacion;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Observacion createUpdatedEntity(EntityManager em) {
        Observacion updatedObservacion = new Observacion()
            .contenido(UPDATED_CONTENIDO)
            .tipo(UPDATED_TIPO)
            .resuelta(UPDATED_RESUELTA)
            .fechaCreacion(UPDATED_FECHA_CREACION);
        // Add required entity
        Entrega entrega;
        if (TestUtil.findAll(em, Entrega.class).isEmpty()) {
            entrega = EntregaResourceIT.createUpdatedEntity(em);
            em.persist(entrega);
            em.flush();
        } else {
            entrega = TestUtil.findAll(em, Entrega.class).getFirst();
        }
        updatedObservacion.setEntrega(entrega);
        return updatedObservacion;
    }

    @BeforeEach
    void initTest() {
        observacion = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedObservacion != null) {
            observacionRepository.delete(insertedObservacion);
            insertedObservacion = null;
        }
    }

    @Test
    @Transactional
    void createObservacion() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Observacion
        ObservacionDTO observacionDTO = observacionMapper.toDto(observacion);
        var returnedObservacionDTO = om.readValue(
            restObservacionMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(observacionDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ObservacionDTO.class
        );

        // Validate the Observacion in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedObservacion = observacionMapper.toEntity(returnedObservacionDTO);
        assertObservacionUpdatableFieldsEquals(returnedObservacion, getPersistedObservacion(returnedObservacion));

        insertedObservacion = returnedObservacion;
    }

    @Test
    @Transactional
    void createObservacionWithExistingId() throws Exception {
        // Create the Observacion with an existing ID
        observacion.setId(1L);
        ObservacionDTO observacionDTO = observacionMapper.toDto(observacion);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restObservacionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(observacionDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Observacion in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTipoIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        observacion.setTipo(null);

        // Create the Observacion, which fails.
        ObservacionDTO observacionDTO = observacionMapper.toDto(observacion);

        restObservacionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(observacionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllObservacions() throws Exception {
        // Initialize the database
        insertedObservacion = observacionRepository.saveAndFlush(observacion);

        // Get all the observacionList
        restObservacionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(observacion.getId().intValue())))
            .andExpect(jsonPath("$.[*].contenido").value(hasItem(DEFAULT_CONTENIDO)))
            .andExpect(jsonPath("$.[*].tipo").value(hasItem(DEFAULT_TIPO.toString())))
            .andExpect(jsonPath("$.[*].resuelta").value(hasItem(DEFAULT_RESUELTA)))
            .andExpect(jsonPath("$.[*].fechaCreacion").value(hasItem(DEFAULT_FECHA_CREACION.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllObservacionsWithEagerRelationshipsIsEnabled() throws Exception {
        when(observacionServiceMock.findAllWithEagerRelationships()).thenReturn(new ArrayList<>());

        restObservacionMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(observacionServiceMock, times(1)).findAllWithEagerRelationships();
    }

    @SuppressWarnings({ "unchecked" })
    void getAllObservacionsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(observacionServiceMock.findAllWithEagerRelationships()).thenReturn(new ArrayList<>());

        restObservacionMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(observacionRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getObservacion() throws Exception {
        // Initialize the database
        insertedObservacion = observacionRepository.saveAndFlush(observacion);

        // Get the observacion
        restObservacionMockMvc
            .perform(get(ENTITY_API_URL_ID, observacion.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(observacion.getId().intValue()))
            .andExpect(jsonPath("$.contenido").value(DEFAULT_CONTENIDO))
            .andExpect(jsonPath("$.tipo").value(DEFAULT_TIPO.toString()))
            .andExpect(jsonPath("$.resuelta").value(DEFAULT_RESUELTA))
            .andExpect(jsonPath("$.fechaCreacion").value(DEFAULT_FECHA_CREACION.toString()));
    }

    @Test
    @Transactional
    void getNonExistingObservacion() throws Exception {
        // Get the observacion
        restObservacionMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNonExistingObservacion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        observacion.setId(longCount.incrementAndGet());

        // Create the Observacion
        ObservacionDTO observacionDTO = observacionMapper.toDto(observacion);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restObservacionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, observacionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(observacionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Observacion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchObservacion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        observacion.setId(longCount.incrementAndGet());

        // Create the Observacion
        ObservacionDTO observacionDTO = observacionMapper.toDto(observacion);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restObservacionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(observacionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Observacion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamObservacion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        observacion.setId(longCount.incrementAndGet());

        // Create the Observacion
        ObservacionDTO observacionDTO = observacionMapper.toDto(observacion);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restObservacionMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(observacionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Observacion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchNonExistingObservacion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        observacion.setId(longCount.incrementAndGet());

        // Create the Observacion
        ObservacionDTO observacionDTO = observacionMapper.toDto(observacion);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restObservacionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, observacionDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(observacionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Observacion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchObservacion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        observacion.setId(longCount.incrementAndGet());

        // Create the Observacion
        ObservacionDTO observacionDTO = observacionMapper.toDto(observacion);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restObservacionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(observacionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Observacion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamObservacion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        observacion.setId(longCount.incrementAndGet());

        // Create the Observacion
        ObservacionDTO observacionDTO = observacionMapper.toDto(observacion);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restObservacionMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(observacionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Observacion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteObservacion() throws Exception {
        // Initialize the database
        insertedObservacion = observacionRepository.saveAndFlush(observacion);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the observacion
        restObservacionMockMvc
            .perform(delete(ENTITY_API_URL_ID, observacion.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return observacionRepository.count();
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

    protected Observacion getPersistedObservacion(Observacion observacion) {
        return observacionRepository.findById(observacion.getId()).orElseThrow();
    }

    protected void assertPersistedObservacionToMatchAllProperties(Observacion expectedObservacion) {
        assertObservacionAllPropertiesEquals(expectedObservacion, getPersistedObservacion(expectedObservacion));
    }

    protected void assertPersistedObservacionToMatchUpdatableProperties(Observacion expectedObservacion) {
        assertObservacionAllUpdatablePropertiesEquals(expectedObservacion, getPersistedObservacion(expectedObservacion));
    }
}
