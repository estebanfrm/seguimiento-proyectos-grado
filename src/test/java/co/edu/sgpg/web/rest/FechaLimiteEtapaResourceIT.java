package co.edu.sgpg.web.rest;

import static co.edu.sgpg.domain.FechaLimiteEtapaAsserts.*;
import static co.edu.sgpg.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import co.edu.sgpg.IntegrationTest;
import co.edu.sgpg.domain.FechaLimiteEtapa;
import co.edu.sgpg.domain.ProyectoGrado;
import co.edu.sgpg.domain.enumeration.Etapa;
import co.edu.sgpg.repository.FechaLimiteEtapaRepository;
import co.edu.sgpg.service.FechaLimiteEtapaService;
import co.edu.sgpg.service.dto.FechaLimiteEtapaDTO;
import co.edu.sgpg.service.mapper.FechaLimiteEtapaMapper;
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
 * Integration tests for the {@link FechaLimiteEtapaResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser(username = "admin", authorities = co.edu.sgpg.security.AuthoritiesConstants.ADMIN)
class FechaLimiteEtapaResourceIT {

    private static final Etapa DEFAULT_ETAPA = Etapa.PROPUESTA;
    private static final Etapa UPDATED_ETAPA = Etapa.ANTEPROYECTO;

    private static final Instant DEFAULT_FECHA_LIMITE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_FECHA_LIMITE = Instant.ofEpochMilli(1701983268847L);

    private static final String DEFAULT_DESCRIPCION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPCION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/fecha-limite-etapas";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private FechaLimiteEtapaRepository fechaLimiteEtapaRepository;

    @Mock
    private FechaLimiteEtapaRepository fechaLimiteEtapaRepositoryMock;

    @Autowired
    private FechaLimiteEtapaMapper fechaLimiteEtapaMapper;

    @Mock
    private FechaLimiteEtapaService fechaLimiteEtapaServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restFechaLimiteEtapaMockMvc;

    private FechaLimiteEtapa fechaLimiteEtapa;

    private FechaLimiteEtapa insertedFechaLimiteEtapa;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static FechaLimiteEtapa createEntity(EntityManager em) {
        FechaLimiteEtapa fechaLimiteEtapa = new FechaLimiteEtapa()
            .etapa(DEFAULT_ETAPA)
            .fechaLimite(DEFAULT_FECHA_LIMITE)
            .descripcion(DEFAULT_DESCRIPCION);
        // Add required entity
        ProyectoGrado proyectoGrado;
        if (TestUtil.findAll(em, ProyectoGrado.class).isEmpty()) {
            proyectoGrado = ProyectoGradoResourceIT.createEntity();
            em.persist(proyectoGrado);
            em.flush();
        } else {
            proyectoGrado = TestUtil.findAll(em, ProyectoGrado.class).getFirst();
        }
        fechaLimiteEtapa.setProyecto(proyectoGrado);
        return fechaLimiteEtapa;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static FechaLimiteEtapa createUpdatedEntity(EntityManager em) {
        FechaLimiteEtapa updatedFechaLimiteEtapa = new FechaLimiteEtapa()
            .etapa(UPDATED_ETAPA)
            .fechaLimite(UPDATED_FECHA_LIMITE)
            .descripcion(UPDATED_DESCRIPCION);
        // Add required entity
        ProyectoGrado proyectoGrado;
        if (TestUtil.findAll(em, ProyectoGrado.class).isEmpty()) {
            proyectoGrado = ProyectoGradoResourceIT.createUpdatedEntity();
            em.persist(proyectoGrado);
            em.flush();
        } else {
            proyectoGrado = TestUtil.findAll(em, ProyectoGrado.class).getFirst();
        }
        updatedFechaLimiteEtapa.setProyecto(proyectoGrado);
        return updatedFechaLimiteEtapa;
    }

    @BeforeEach
    void initTest() {
        fechaLimiteEtapa = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedFechaLimiteEtapa != null) {
            fechaLimiteEtapaRepository.delete(insertedFechaLimiteEtapa);
            insertedFechaLimiteEtapa = null;
        }
    }

    @Test
    @Transactional
    void createFechaLimiteEtapa() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the FechaLimiteEtapa
        FechaLimiteEtapaDTO fechaLimiteEtapaDTO = fechaLimiteEtapaMapper.toDto(fechaLimiteEtapa);
        var returnedFechaLimiteEtapaDTO = om.readValue(
            restFechaLimiteEtapaMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(fechaLimiteEtapaDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            FechaLimiteEtapaDTO.class
        );

        // Validate the FechaLimiteEtapa in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedFechaLimiteEtapa = fechaLimiteEtapaMapper.toEntity(returnedFechaLimiteEtapaDTO);
        assertFechaLimiteEtapaUpdatableFieldsEquals(returnedFechaLimiteEtapa, getPersistedFechaLimiteEtapa(returnedFechaLimiteEtapa));

        insertedFechaLimiteEtapa = returnedFechaLimiteEtapa;
    }

    @Test
    @Transactional
    void createFechaLimiteEtapaWithExistingId() throws Exception {
        // Create the FechaLimiteEtapa with an existing ID
        fechaLimiteEtapa.setId(1L);
        FechaLimiteEtapaDTO fechaLimiteEtapaDTO = fechaLimiteEtapaMapper.toDto(fechaLimiteEtapa);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restFechaLimiteEtapaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(fechaLimiteEtapaDTO)))
            .andExpect(status().isBadRequest());

        // Validate the FechaLimiteEtapa in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkEtapaIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        fechaLimiteEtapa.setEtapa(null);

        // Create the FechaLimiteEtapa, which fails.
        FechaLimiteEtapaDTO fechaLimiteEtapaDTO = fechaLimiteEtapaMapper.toDto(fechaLimiteEtapa);

        restFechaLimiteEtapaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(fechaLimiteEtapaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkFechaLimiteIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        fechaLimiteEtapa.setFechaLimite(null);

        // Create the FechaLimiteEtapa, which fails.
        FechaLimiteEtapaDTO fechaLimiteEtapaDTO = fechaLimiteEtapaMapper.toDto(fechaLimiteEtapa);

        restFechaLimiteEtapaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(fechaLimiteEtapaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllFechaLimiteEtapas() throws Exception {
        // Initialize the database
        insertedFechaLimiteEtapa = fechaLimiteEtapaRepository.saveAndFlush(fechaLimiteEtapa);

        // Get all the fechaLimiteEtapaList
        restFechaLimiteEtapaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(fechaLimiteEtapa.getId().intValue())))
            .andExpect(jsonPath("$.[*].etapa").value(hasItem(DEFAULT_ETAPA.toString())))
            .andExpect(jsonPath("$.[*].fechaLimite").value(hasItem(DEFAULT_FECHA_LIMITE.toString())))
            .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllFechaLimiteEtapasWithEagerRelationshipsIsEnabled() throws Exception {
        when(fechaLimiteEtapaServiceMock.findAllWithEagerRelationships()).thenReturn(new ArrayList<>());

        restFechaLimiteEtapaMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(fechaLimiteEtapaServiceMock, times(1)).findAllWithEagerRelationships();
    }

    @SuppressWarnings({ "unchecked" })
    void getAllFechaLimiteEtapasWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(fechaLimiteEtapaServiceMock.findAllWithEagerRelationships()).thenReturn(new ArrayList<>());

        restFechaLimiteEtapaMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(fechaLimiteEtapaRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getFechaLimiteEtapa() throws Exception {
        // Initialize the database
        insertedFechaLimiteEtapa = fechaLimiteEtapaRepository.saveAndFlush(fechaLimiteEtapa);

        // Get the fechaLimiteEtapa
        restFechaLimiteEtapaMockMvc
            .perform(get(ENTITY_API_URL_ID, fechaLimiteEtapa.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(fechaLimiteEtapa.getId().intValue()))
            .andExpect(jsonPath("$.etapa").value(DEFAULT_ETAPA.toString()))
            .andExpect(jsonPath("$.fechaLimite").value(DEFAULT_FECHA_LIMITE.toString()))
            .andExpect(jsonPath("$.descripcion").value(DEFAULT_DESCRIPCION));
    }

    @Test
    @Transactional
    void getNonExistingFechaLimiteEtapa() throws Exception {
        // Get the fechaLimiteEtapa
        restFechaLimiteEtapaMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingFechaLimiteEtapa() throws Exception {
        // Initialize the database
        insertedFechaLimiteEtapa = fechaLimiteEtapaRepository.saveAndFlush(fechaLimiteEtapa);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the fechaLimiteEtapa
        FechaLimiteEtapa updatedFechaLimiteEtapa = fechaLimiteEtapaRepository.findById(fechaLimiteEtapa.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedFechaLimiteEtapa are not directly saved in db
        em.detach(updatedFechaLimiteEtapa);
        updatedFechaLimiteEtapa.etapa(UPDATED_ETAPA).fechaLimite(UPDATED_FECHA_LIMITE).descripcion(UPDATED_DESCRIPCION);
        FechaLimiteEtapaDTO fechaLimiteEtapaDTO = fechaLimiteEtapaMapper.toDto(updatedFechaLimiteEtapa);

        restFechaLimiteEtapaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, fechaLimiteEtapaDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(fechaLimiteEtapaDTO))
            )
            .andExpect(status().isOk());

        // Validate the FechaLimiteEtapa in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedFechaLimiteEtapaToMatchAllProperties(updatedFechaLimiteEtapa);
    }

    @Test
    @Transactional
    void putNonExistingFechaLimiteEtapa() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        fechaLimiteEtapa.setId(longCount.incrementAndGet());

        // Create the FechaLimiteEtapa
        FechaLimiteEtapaDTO fechaLimiteEtapaDTO = fechaLimiteEtapaMapper.toDto(fechaLimiteEtapa);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFechaLimiteEtapaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, fechaLimiteEtapaDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(fechaLimiteEtapaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the FechaLimiteEtapa in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchFechaLimiteEtapa() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        fechaLimiteEtapa.setId(longCount.incrementAndGet());

        // Create the FechaLimiteEtapa
        FechaLimiteEtapaDTO fechaLimiteEtapaDTO = fechaLimiteEtapaMapper.toDto(fechaLimiteEtapa);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFechaLimiteEtapaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(fechaLimiteEtapaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the FechaLimiteEtapa in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamFechaLimiteEtapa() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        fechaLimiteEtapa.setId(longCount.incrementAndGet());

        // Create the FechaLimiteEtapa
        FechaLimiteEtapaDTO fechaLimiteEtapaDTO = fechaLimiteEtapaMapper.toDto(fechaLimiteEtapa);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFechaLimiteEtapaMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(fechaLimiteEtapaDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the FechaLimiteEtapa in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateFechaLimiteEtapaWithPatch() throws Exception {
        // Initialize the database
        insertedFechaLimiteEtapa = fechaLimiteEtapaRepository.saveAndFlush(fechaLimiteEtapa);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the fechaLimiteEtapa using partial update
        FechaLimiteEtapa partialUpdatedFechaLimiteEtapa = new FechaLimiteEtapa();
        partialUpdatedFechaLimiteEtapa.setId(fechaLimiteEtapa.getId());

        partialUpdatedFechaLimiteEtapa.etapa(UPDATED_ETAPA);

        restFechaLimiteEtapaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedFechaLimiteEtapa.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedFechaLimiteEtapa))
            )
            .andExpect(status().isOk());

        // Validate the FechaLimiteEtapa in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertFechaLimiteEtapaUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedFechaLimiteEtapa, fechaLimiteEtapa),
            getPersistedFechaLimiteEtapa(fechaLimiteEtapa)
        );
    }

    @Test
    @Transactional
    void fullUpdateFechaLimiteEtapaWithPatch() throws Exception {
        // Initialize the database
        insertedFechaLimiteEtapa = fechaLimiteEtapaRepository.saveAndFlush(fechaLimiteEtapa);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the fechaLimiteEtapa using partial update
        FechaLimiteEtapa partialUpdatedFechaLimiteEtapa = new FechaLimiteEtapa();
        partialUpdatedFechaLimiteEtapa.setId(fechaLimiteEtapa.getId());

        partialUpdatedFechaLimiteEtapa.etapa(UPDATED_ETAPA).fechaLimite(UPDATED_FECHA_LIMITE).descripcion(UPDATED_DESCRIPCION);

        restFechaLimiteEtapaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedFechaLimiteEtapa.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedFechaLimiteEtapa))
            )
            .andExpect(status().isOk());

        // Validate the FechaLimiteEtapa in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertFechaLimiteEtapaUpdatableFieldsEquals(
            partialUpdatedFechaLimiteEtapa,
            getPersistedFechaLimiteEtapa(partialUpdatedFechaLimiteEtapa)
        );
    }

    @Test
    @Transactional
    void patchNonExistingFechaLimiteEtapa() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        fechaLimiteEtapa.setId(longCount.incrementAndGet());

        // Create the FechaLimiteEtapa
        FechaLimiteEtapaDTO fechaLimiteEtapaDTO = fechaLimiteEtapaMapper.toDto(fechaLimiteEtapa);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFechaLimiteEtapaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, fechaLimiteEtapaDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(fechaLimiteEtapaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the FechaLimiteEtapa in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchFechaLimiteEtapa() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        fechaLimiteEtapa.setId(longCount.incrementAndGet());

        // Create the FechaLimiteEtapa
        FechaLimiteEtapaDTO fechaLimiteEtapaDTO = fechaLimiteEtapaMapper.toDto(fechaLimiteEtapa);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFechaLimiteEtapaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(fechaLimiteEtapaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the FechaLimiteEtapa in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamFechaLimiteEtapa() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        fechaLimiteEtapa.setId(longCount.incrementAndGet());

        // Create the FechaLimiteEtapa
        FechaLimiteEtapaDTO fechaLimiteEtapaDTO = fechaLimiteEtapaMapper.toDto(fechaLimiteEtapa);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFechaLimiteEtapaMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(fechaLimiteEtapaDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the FechaLimiteEtapa in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteFechaLimiteEtapa() throws Exception {
        // Initialize the database
        insertedFechaLimiteEtapa = fechaLimiteEtapaRepository.saveAndFlush(fechaLimiteEtapa);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the fechaLimiteEtapa
        restFechaLimiteEtapaMockMvc
            .perform(delete(ENTITY_API_URL_ID, fechaLimiteEtapa.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return fechaLimiteEtapaRepository.count();
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

    protected FechaLimiteEtapa getPersistedFechaLimiteEtapa(FechaLimiteEtapa fechaLimiteEtapa) {
        return fechaLimiteEtapaRepository.findById(fechaLimiteEtapa.getId()).orElseThrow();
    }

    protected void assertPersistedFechaLimiteEtapaToMatchAllProperties(FechaLimiteEtapa expectedFechaLimiteEtapa) {
        assertFechaLimiteEtapaAllPropertiesEquals(expectedFechaLimiteEtapa, getPersistedFechaLimiteEtapa(expectedFechaLimiteEtapa));
    }

    protected void assertPersistedFechaLimiteEtapaToMatchUpdatableProperties(FechaLimiteEtapa expectedFechaLimiteEtapa) {
        assertFechaLimiteEtapaAllUpdatablePropertiesEquals(
            expectedFechaLimiteEtapa,
            getPersistedFechaLimiteEtapa(expectedFechaLimiteEtapa)
        );
    }
}
