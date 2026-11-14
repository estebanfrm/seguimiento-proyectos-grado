package co.edu.sgpg.web.rest;

import static co.edu.sgpg.domain.EvaluacionAsserts.*;
import static co.edu.sgpg.web.rest.TestUtil.createUpdateProxyForBean;
import static co.edu.sgpg.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import co.edu.sgpg.IntegrationTest;
import co.edu.sgpg.domain.Evaluacion;
import co.edu.sgpg.domain.ProyectoGrado;
import co.edu.sgpg.domain.enumeration.Concepto;
import co.edu.sgpg.domain.enumeration.Etapa;
import co.edu.sgpg.repository.EvaluacionRepository;
import co.edu.sgpg.repository.UserRepository;
import co.edu.sgpg.service.EvaluacionService;
import co.edu.sgpg.service.dto.EvaluacionDTO;
import co.edu.sgpg.service.mapper.EvaluacionMapper;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
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
 * Integration tests for the {@link EvaluacionResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class EvaluacionResourceIT {

    private static final Etapa DEFAULT_ETAPA = Etapa.PROPUESTA;
    private static final Etapa UPDATED_ETAPA = Etapa.ANTEPROYECTO;

    private static final BigDecimal DEFAULT_CALIFICACION = new BigDecimal(0);
    private static final BigDecimal UPDATED_CALIFICACION = new BigDecimal(1);

    private static final Concepto DEFAULT_CONCEPTO = Concepto.APROBADO;
    private static final Concepto UPDATED_CONCEPTO = Concepto.APROBADO_CON_MODIFICACIONES;

    private static final String DEFAULT_COMENTARIOS = "AAAAAAAAAA";
    private static final String UPDATED_COMENTARIOS = "BBBBBBBBBB";

    private static final Instant DEFAULT_FECHA_EVALUACION = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_FECHA_EVALUACION = Instant.ofEpochMilli(1701983268847L);

    private static final String ENTITY_API_URL = "/api/evaluacions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private EvaluacionRepository evaluacionRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private EvaluacionRepository evaluacionRepositoryMock;

    @Autowired
    private EvaluacionMapper evaluacionMapper;

    @Mock
    private EvaluacionService evaluacionServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restEvaluacionMockMvc;

    private Evaluacion evaluacion;

    private Evaluacion insertedEvaluacion;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Evaluacion createEntity(EntityManager em) {
        Evaluacion evaluacion = new Evaluacion()
            .etapa(DEFAULT_ETAPA)
            .calificacion(DEFAULT_CALIFICACION)
            .concepto(DEFAULT_CONCEPTO)
            .comentarios(DEFAULT_COMENTARIOS)
            .fechaEvaluacion(DEFAULT_FECHA_EVALUACION);
        // Add required entity
        ProyectoGrado proyectoGrado;
        if (TestUtil.findAll(em, ProyectoGrado.class).isEmpty()) {
            proyectoGrado = ProyectoGradoResourceIT.createEntity();
            em.persist(proyectoGrado);
            em.flush();
        } else {
            proyectoGrado = TestUtil.findAll(em, ProyectoGrado.class).getFirst();
        }
        evaluacion.setProyecto(proyectoGrado);
        return evaluacion;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Evaluacion createUpdatedEntity(EntityManager em) {
        Evaluacion updatedEvaluacion = new Evaluacion()
            .etapa(UPDATED_ETAPA)
            .calificacion(UPDATED_CALIFICACION)
            .concepto(UPDATED_CONCEPTO)
            .comentarios(UPDATED_COMENTARIOS)
            .fechaEvaluacion(UPDATED_FECHA_EVALUACION);
        // Add required entity
        ProyectoGrado proyectoGrado;
        if (TestUtil.findAll(em, ProyectoGrado.class).isEmpty()) {
            proyectoGrado = ProyectoGradoResourceIT.createUpdatedEntity();
            em.persist(proyectoGrado);
            em.flush();
        } else {
            proyectoGrado = TestUtil.findAll(em, ProyectoGrado.class).getFirst();
        }
        updatedEvaluacion.setProyecto(proyectoGrado);
        return updatedEvaluacion;
    }

    @BeforeEach
    void initTest() {
        evaluacion = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedEvaluacion != null) {
            evaluacionRepository.delete(insertedEvaluacion);
            insertedEvaluacion = null;
        }
    }

    @Test
    @Transactional
    void createEvaluacion() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Evaluacion
        EvaluacionDTO evaluacionDTO = evaluacionMapper.toDto(evaluacion);
        var returnedEvaluacionDTO = om.readValue(
            restEvaluacionMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(evaluacionDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            EvaluacionDTO.class
        );

        // Validate the Evaluacion in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedEvaluacion = evaluacionMapper.toEntity(returnedEvaluacionDTO);
        assertEvaluacionUpdatableFieldsEquals(returnedEvaluacion, getPersistedEvaluacion(returnedEvaluacion));

        insertedEvaluacion = returnedEvaluacion;
    }

    @Test
    @Transactional
    void createEvaluacionWithExistingId() throws Exception {
        // Create the Evaluacion with an existing ID
        evaluacion.setId(1L);
        EvaluacionDTO evaluacionDTO = evaluacionMapper.toDto(evaluacion);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restEvaluacionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(evaluacionDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Evaluacion in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkEtapaIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        evaluacion.setEtapa(null);

        // Create the Evaluacion, which fails.
        EvaluacionDTO evaluacionDTO = evaluacionMapper.toDto(evaluacion);

        restEvaluacionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(evaluacionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCalificacionIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        evaluacion.setCalificacion(null);

        // Create the Evaluacion, which fails.
        EvaluacionDTO evaluacionDTO = evaluacionMapper.toDto(evaluacion);

        restEvaluacionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(evaluacionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkConceptoIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        evaluacion.setConcepto(null);

        // Create the Evaluacion, which fails.
        EvaluacionDTO evaluacionDTO = evaluacionMapper.toDto(evaluacion);

        restEvaluacionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(evaluacionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkFechaEvaluacionIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        evaluacion.setFechaEvaluacion(null);

        // Create the Evaluacion, which fails.
        EvaluacionDTO evaluacionDTO = evaluacionMapper.toDto(evaluacion);

        restEvaluacionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(evaluacionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllEvaluacions() throws Exception {
        // Initialize the database
        insertedEvaluacion = evaluacionRepository.saveAndFlush(evaluacion);

        // Get all the evaluacionList
        restEvaluacionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(evaluacion.getId().intValue())))
            .andExpect(jsonPath("$.[*].etapa").value(hasItem(DEFAULT_ETAPA.toString())))
            .andExpect(jsonPath("$.[*].calificacion").value(hasItem(sameNumber(DEFAULT_CALIFICACION))))
            .andExpect(jsonPath("$.[*].concepto").value(hasItem(DEFAULT_CONCEPTO.toString())))
            .andExpect(jsonPath("$.[*].comentarios").value(hasItem(DEFAULT_COMENTARIOS)))
            .andExpect(jsonPath("$.[*].fechaEvaluacion").value(hasItem(DEFAULT_FECHA_EVALUACION.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllEvaluacionsWithEagerRelationshipsIsEnabled() throws Exception {
        when(evaluacionServiceMock.findAllWithEagerRelationships()).thenReturn(new ArrayList<>());

        restEvaluacionMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(evaluacionServiceMock, times(1)).findAllWithEagerRelationships();
    }

    @SuppressWarnings({ "unchecked" })
    void getAllEvaluacionsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(evaluacionServiceMock.findAllWithEagerRelationships()).thenReturn(new ArrayList<>());

        restEvaluacionMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(evaluacionRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getEvaluacion() throws Exception {
        // Initialize the database
        insertedEvaluacion = evaluacionRepository.saveAndFlush(evaluacion);

        // Get the evaluacion
        restEvaluacionMockMvc
            .perform(get(ENTITY_API_URL_ID, evaluacion.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(evaluacion.getId().intValue()))
            .andExpect(jsonPath("$.etapa").value(DEFAULT_ETAPA.toString()))
            .andExpect(jsonPath("$.calificacion").value(sameNumber(DEFAULT_CALIFICACION)))
            .andExpect(jsonPath("$.concepto").value(DEFAULT_CONCEPTO.toString()))
            .andExpect(jsonPath("$.comentarios").value(DEFAULT_COMENTARIOS))
            .andExpect(jsonPath("$.fechaEvaluacion").value(DEFAULT_FECHA_EVALUACION.toString()));
    }

    @Test
    @Transactional
    void getNonExistingEvaluacion() throws Exception {
        // Get the evaluacion
        restEvaluacionMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingEvaluacion() throws Exception {
        // Initialize the database
        insertedEvaluacion = evaluacionRepository.saveAndFlush(evaluacion);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the evaluacion
        Evaluacion updatedEvaluacion = evaluacionRepository.findById(evaluacion.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedEvaluacion are not directly saved in db
        em.detach(updatedEvaluacion);
        updatedEvaluacion
            .etapa(UPDATED_ETAPA)
            .calificacion(UPDATED_CALIFICACION)
            .concepto(UPDATED_CONCEPTO)
            .comentarios(UPDATED_COMENTARIOS)
            .fechaEvaluacion(UPDATED_FECHA_EVALUACION);
        EvaluacionDTO evaluacionDTO = evaluacionMapper.toDto(updatedEvaluacion);

        restEvaluacionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, evaluacionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(evaluacionDTO))
            )
            .andExpect(status().isOk());

        // Validate the Evaluacion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedEvaluacionToMatchAllProperties(updatedEvaluacion);
    }

    @Test
    @Transactional
    void putNonExistingEvaluacion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        evaluacion.setId(longCount.incrementAndGet());

        // Create the Evaluacion
        EvaluacionDTO evaluacionDTO = evaluacionMapper.toDto(evaluacion);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restEvaluacionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, evaluacionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(evaluacionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Evaluacion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchEvaluacion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        evaluacion.setId(longCount.incrementAndGet());

        // Create the Evaluacion
        EvaluacionDTO evaluacionDTO = evaluacionMapper.toDto(evaluacion);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEvaluacionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(evaluacionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Evaluacion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamEvaluacion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        evaluacion.setId(longCount.incrementAndGet());

        // Create the Evaluacion
        EvaluacionDTO evaluacionDTO = evaluacionMapper.toDto(evaluacion);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEvaluacionMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(evaluacionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Evaluacion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateEvaluacionWithPatch() throws Exception {
        // Initialize the database
        insertedEvaluacion = evaluacionRepository.saveAndFlush(evaluacion);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the evaluacion using partial update
        Evaluacion partialUpdatedEvaluacion = new Evaluacion();
        partialUpdatedEvaluacion.setId(evaluacion.getId());

        partialUpdatedEvaluacion.comentarios(UPDATED_COMENTARIOS);

        restEvaluacionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedEvaluacion.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedEvaluacion))
            )
            .andExpect(status().isOk());

        // Validate the Evaluacion in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertEvaluacionUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedEvaluacion, evaluacion),
            getPersistedEvaluacion(evaluacion)
        );
    }

    @Test
    @Transactional
    void fullUpdateEvaluacionWithPatch() throws Exception {
        // Initialize the database
        insertedEvaluacion = evaluacionRepository.saveAndFlush(evaluacion);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the evaluacion using partial update
        Evaluacion partialUpdatedEvaluacion = new Evaluacion();
        partialUpdatedEvaluacion.setId(evaluacion.getId());

        partialUpdatedEvaluacion
            .etapa(UPDATED_ETAPA)
            .calificacion(UPDATED_CALIFICACION)
            .concepto(UPDATED_CONCEPTO)
            .comentarios(UPDATED_COMENTARIOS)
            .fechaEvaluacion(UPDATED_FECHA_EVALUACION);

        restEvaluacionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedEvaluacion.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedEvaluacion))
            )
            .andExpect(status().isOk());

        // Validate the Evaluacion in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertEvaluacionUpdatableFieldsEquals(partialUpdatedEvaluacion, getPersistedEvaluacion(partialUpdatedEvaluacion));
    }

    @Test
    @Transactional
    void patchNonExistingEvaluacion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        evaluacion.setId(longCount.incrementAndGet());

        // Create the Evaluacion
        EvaluacionDTO evaluacionDTO = evaluacionMapper.toDto(evaluacion);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restEvaluacionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, evaluacionDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(evaluacionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Evaluacion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchEvaluacion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        evaluacion.setId(longCount.incrementAndGet());

        // Create the Evaluacion
        EvaluacionDTO evaluacionDTO = evaluacionMapper.toDto(evaluacion);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEvaluacionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(evaluacionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Evaluacion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamEvaluacion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        evaluacion.setId(longCount.incrementAndGet());

        // Create the Evaluacion
        EvaluacionDTO evaluacionDTO = evaluacionMapper.toDto(evaluacion);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEvaluacionMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(evaluacionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Evaluacion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteEvaluacion() throws Exception {
        // Initialize the database
        insertedEvaluacion = evaluacionRepository.saveAndFlush(evaluacion);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the evaluacion
        restEvaluacionMockMvc
            .perform(delete(ENTITY_API_URL_ID, evaluacion.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return evaluacionRepository.count();
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

    protected Evaluacion getPersistedEvaluacion(Evaluacion evaluacion) {
        return evaluacionRepository.findById(evaluacion.getId()).orElseThrow();
    }

    protected void assertPersistedEvaluacionToMatchAllProperties(Evaluacion expectedEvaluacion) {
        assertEvaluacionAllPropertiesEquals(expectedEvaluacion, getPersistedEvaluacion(expectedEvaluacion));
    }

    protected void assertPersistedEvaluacionToMatchUpdatableProperties(Evaluacion expectedEvaluacion) {
        assertEvaluacionAllUpdatablePropertiesEquals(expectedEvaluacion, getPersistedEvaluacion(expectedEvaluacion));
    }
}
