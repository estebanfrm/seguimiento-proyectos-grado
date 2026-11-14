package co.edu.sgpg.web.rest;

import static co.edu.sgpg.domain.HistorialEtapaAsserts.*;
import static co.edu.sgpg.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import co.edu.sgpg.IntegrationTest;
import co.edu.sgpg.domain.HistorialEtapa;
import co.edu.sgpg.domain.ProyectoGrado;
import co.edu.sgpg.domain.enumeration.Etapa;
import co.edu.sgpg.domain.enumeration.Etapa;
import co.edu.sgpg.repository.HistorialEtapaRepository;
import co.edu.sgpg.repository.UserRepository;
import co.edu.sgpg.service.HistorialEtapaService;
import co.edu.sgpg.service.dto.HistorialEtapaDTO;
import co.edu.sgpg.service.mapper.HistorialEtapaMapper;
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
 * Integration tests for the {@link HistorialEtapaResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class HistorialEtapaResourceIT {

    private static final Etapa DEFAULT_ETAPA_ORIGEN = Etapa.PROPUESTA;
    private static final Etapa UPDATED_ETAPA_ORIGEN = Etapa.ANTEPROYECTO;

    private static final Etapa DEFAULT_ETAPA_DESTINO = Etapa.PROPUESTA;
    private static final Etapa UPDATED_ETAPA_DESTINO = Etapa.ANTEPROYECTO;

    private static final Instant DEFAULT_FECHA = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_FECHA = Instant.ofEpochMilli(1701983268847L);

    private static final String DEFAULT_COMENTARIO = "AAAAAAAAAA";
    private static final String UPDATED_COMENTARIO = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/historial-etapas";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private HistorialEtapaRepository historialEtapaRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private HistorialEtapaRepository historialEtapaRepositoryMock;

    @Autowired
    private HistorialEtapaMapper historialEtapaMapper;

    @Mock
    private HistorialEtapaService historialEtapaServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restHistorialEtapaMockMvc;

    private HistorialEtapa historialEtapa;

    private HistorialEtapa insertedHistorialEtapa;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static HistorialEtapa createEntity(EntityManager em) {
        HistorialEtapa historialEtapa = new HistorialEtapa()
            .etapaOrigen(DEFAULT_ETAPA_ORIGEN)
            .etapaDestino(DEFAULT_ETAPA_DESTINO)
            .fecha(DEFAULT_FECHA)
            .comentario(DEFAULT_COMENTARIO);
        // Add required entity
        ProyectoGrado proyectoGrado;
        if (TestUtil.findAll(em, ProyectoGrado.class).isEmpty()) {
            proyectoGrado = ProyectoGradoResourceIT.createEntity();
            em.persist(proyectoGrado);
            em.flush();
        } else {
            proyectoGrado = TestUtil.findAll(em, ProyectoGrado.class).getFirst();
        }
        historialEtapa.setProyecto(proyectoGrado);
        return historialEtapa;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static HistorialEtapa createUpdatedEntity(EntityManager em) {
        HistorialEtapa updatedHistorialEtapa = new HistorialEtapa()
            .etapaOrigen(UPDATED_ETAPA_ORIGEN)
            .etapaDestino(UPDATED_ETAPA_DESTINO)
            .fecha(UPDATED_FECHA)
            .comentario(UPDATED_COMENTARIO);
        // Add required entity
        ProyectoGrado proyectoGrado;
        if (TestUtil.findAll(em, ProyectoGrado.class).isEmpty()) {
            proyectoGrado = ProyectoGradoResourceIT.createUpdatedEntity();
            em.persist(proyectoGrado);
            em.flush();
        } else {
            proyectoGrado = TestUtil.findAll(em, ProyectoGrado.class).getFirst();
        }
        updatedHistorialEtapa.setProyecto(proyectoGrado);
        return updatedHistorialEtapa;
    }

    @BeforeEach
    void initTest() {
        historialEtapa = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedHistorialEtapa != null) {
            historialEtapaRepository.delete(insertedHistorialEtapa);
            insertedHistorialEtapa = null;
        }
    }

    @Test
    @Transactional
    void createHistorialEtapa() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the HistorialEtapa
        HistorialEtapaDTO historialEtapaDTO = historialEtapaMapper.toDto(historialEtapa);
        var returnedHistorialEtapaDTO = om.readValue(
            restHistorialEtapaMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(historialEtapaDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            HistorialEtapaDTO.class
        );

        // Validate the HistorialEtapa in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedHistorialEtapa = historialEtapaMapper.toEntity(returnedHistorialEtapaDTO);
        assertHistorialEtapaUpdatableFieldsEquals(returnedHistorialEtapa, getPersistedHistorialEtapa(returnedHistorialEtapa));

        insertedHistorialEtapa = returnedHistorialEtapa;
    }

    @Test
    @Transactional
    void createHistorialEtapaWithExistingId() throws Exception {
        // Create the HistorialEtapa with an existing ID
        historialEtapa.setId(1L);
        HistorialEtapaDTO historialEtapaDTO = historialEtapaMapper.toDto(historialEtapa);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restHistorialEtapaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(historialEtapaDTO)))
            .andExpect(status().isBadRequest());

        // Validate the HistorialEtapa in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkEtapaDestinoIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        historialEtapa.setEtapaDestino(null);

        // Create the HistorialEtapa, which fails.
        HistorialEtapaDTO historialEtapaDTO = historialEtapaMapper.toDto(historialEtapa);

        restHistorialEtapaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(historialEtapaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkFechaIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        historialEtapa.setFecha(null);

        // Create the HistorialEtapa, which fails.
        HistorialEtapaDTO historialEtapaDTO = historialEtapaMapper.toDto(historialEtapa);

        restHistorialEtapaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(historialEtapaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllHistorialEtapas() throws Exception {
        // Initialize the database
        insertedHistorialEtapa = historialEtapaRepository.saveAndFlush(historialEtapa);

        // Get all the historialEtapaList
        restHistorialEtapaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(historialEtapa.getId().intValue())))
            .andExpect(jsonPath("$.[*].etapaOrigen").value(hasItem(DEFAULT_ETAPA_ORIGEN.toString())))
            .andExpect(jsonPath("$.[*].etapaDestino").value(hasItem(DEFAULT_ETAPA_DESTINO.toString())))
            .andExpect(jsonPath("$.[*].fecha").value(hasItem(DEFAULT_FECHA.toString())))
            .andExpect(jsonPath("$.[*].comentario").value(hasItem(DEFAULT_COMENTARIO)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllHistorialEtapasWithEagerRelationshipsIsEnabled() throws Exception {
        when(historialEtapaServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restHistorialEtapaMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(historialEtapaServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllHistorialEtapasWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(historialEtapaServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restHistorialEtapaMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(historialEtapaRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getHistorialEtapa() throws Exception {
        // Initialize the database
        insertedHistorialEtapa = historialEtapaRepository.saveAndFlush(historialEtapa);

        // Get the historialEtapa
        restHistorialEtapaMockMvc
            .perform(get(ENTITY_API_URL_ID, historialEtapa.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(historialEtapa.getId().intValue()))
            .andExpect(jsonPath("$.etapaOrigen").value(DEFAULT_ETAPA_ORIGEN.toString()))
            .andExpect(jsonPath("$.etapaDestino").value(DEFAULT_ETAPA_DESTINO.toString()))
            .andExpect(jsonPath("$.fecha").value(DEFAULT_FECHA.toString()))
            .andExpect(jsonPath("$.comentario").value(DEFAULT_COMENTARIO));
    }

    @Test
    @Transactional
    void getNonExistingHistorialEtapa() throws Exception {
        // Get the historialEtapa
        restHistorialEtapaMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingHistorialEtapa() throws Exception {
        // Initialize the database
        insertedHistorialEtapa = historialEtapaRepository.saveAndFlush(historialEtapa);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the historialEtapa
        HistorialEtapa updatedHistorialEtapa = historialEtapaRepository.findById(historialEtapa.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedHistorialEtapa are not directly saved in db
        em.detach(updatedHistorialEtapa);
        updatedHistorialEtapa
            .etapaOrigen(UPDATED_ETAPA_ORIGEN)
            .etapaDestino(UPDATED_ETAPA_DESTINO)
            .fecha(UPDATED_FECHA)
            .comentario(UPDATED_COMENTARIO);
        HistorialEtapaDTO historialEtapaDTO = historialEtapaMapper.toDto(updatedHistorialEtapa);

        restHistorialEtapaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, historialEtapaDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(historialEtapaDTO))
            )
            .andExpect(status().isOk());

        // Validate the HistorialEtapa in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedHistorialEtapaToMatchAllProperties(updatedHistorialEtapa);
    }

    @Test
    @Transactional
    void putNonExistingHistorialEtapa() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        historialEtapa.setId(longCount.incrementAndGet());

        // Create the HistorialEtapa
        HistorialEtapaDTO historialEtapaDTO = historialEtapaMapper.toDto(historialEtapa);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restHistorialEtapaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, historialEtapaDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(historialEtapaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the HistorialEtapa in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchHistorialEtapa() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        historialEtapa.setId(longCount.incrementAndGet());

        // Create the HistorialEtapa
        HistorialEtapaDTO historialEtapaDTO = historialEtapaMapper.toDto(historialEtapa);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restHistorialEtapaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(historialEtapaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the HistorialEtapa in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamHistorialEtapa() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        historialEtapa.setId(longCount.incrementAndGet());

        // Create the HistorialEtapa
        HistorialEtapaDTO historialEtapaDTO = historialEtapaMapper.toDto(historialEtapa);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restHistorialEtapaMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(historialEtapaDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the HistorialEtapa in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateHistorialEtapaWithPatch() throws Exception {
        // Initialize the database
        insertedHistorialEtapa = historialEtapaRepository.saveAndFlush(historialEtapa);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the historialEtapa using partial update
        HistorialEtapa partialUpdatedHistorialEtapa = new HistorialEtapa();
        partialUpdatedHistorialEtapa.setId(historialEtapa.getId());

        partialUpdatedHistorialEtapa.fecha(UPDATED_FECHA).comentario(UPDATED_COMENTARIO);

        restHistorialEtapaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedHistorialEtapa.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedHistorialEtapa))
            )
            .andExpect(status().isOk());

        // Validate the HistorialEtapa in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertHistorialEtapaUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedHistorialEtapa, historialEtapa),
            getPersistedHistorialEtapa(historialEtapa)
        );
    }

    @Test
    @Transactional
    void fullUpdateHistorialEtapaWithPatch() throws Exception {
        // Initialize the database
        insertedHistorialEtapa = historialEtapaRepository.saveAndFlush(historialEtapa);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the historialEtapa using partial update
        HistorialEtapa partialUpdatedHistorialEtapa = new HistorialEtapa();
        partialUpdatedHistorialEtapa.setId(historialEtapa.getId());

        partialUpdatedHistorialEtapa
            .etapaOrigen(UPDATED_ETAPA_ORIGEN)
            .etapaDestino(UPDATED_ETAPA_DESTINO)
            .fecha(UPDATED_FECHA)
            .comentario(UPDATED_COMENTARIO);

        restHistorialEtapaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedHistorialEtapa.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedHistorialEtapa))
            )
            .andExpect(status().isOk());

        // Validate the HistorialEtapa in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertHistorialEtapaUpdatableFieldsEquals(partialUpdatedHistorialEtapa, getPersistedHistorialEtapa(partialUpdatedHistorialEtapa));
    }

    @Test
    @Transactional
    void patchNonExistingHistorialEtapa() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        historialEtapa.setId(longCount.incrementAndGet());

        // Create the HistorialEtapa
        HistorialEtapaDTO historialEtapaDTO = historialEtapaMapper.toDto(historialEtapa);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restHistorialEtapaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, historialEtapaDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(historialEtapaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the HistorialEtapa in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchHistorialEtapa() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        historialEtapa.setId(longCount.incrementAndGet());

        // Create the HistorialEtapa
        HistorialEtapaDTO historialEtapaDTO = historialEtapaMapper.toDto(historialEtapa);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restHistorialEtapaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(historialEtapaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the HistorialEtapa in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamHistorialEtapa() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        historialEtapa.setId(longCount.incrementAndGet());

        // Create the HistorialEtapa
        HistorialEtapaDTO historialEtapaDTO = historialEtapaMapper.toDto(historialEtapa);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restHistorialEtapaMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(historialEtapaDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the HistorialEtapa in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteHistorialEtapa() throws Exception {
        // Initialize the database
        insertedHistorialEtapa = historialEtapaRepository.saveAndFlush(historialEtapa);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the historialEtapa
        restHistorialEtapaMockMvc
            .perform(delete(ENTITY_API_URL_ID, historialEtapa.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return historialEtapaRepository.count();
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

    protected HistorialEtapa getPersistedHistorialEtapa(HistorialEtapa historialEtapa) {
        return historialEtapaRepository.findById(historialEtapa.getId()).orElseThrow();
    }

    protected void assertPersistedHistorialEtapaToMatchAllProperties(HistorialEtapa expectedHistorialEtapa) {
        assertHistorialEtapaAllPropertiesEquals(expectedHistorialEtapa, getPersistedHistorialEtapa(expectedHistorialEtapa));
    }

    protected void assertPersistedHistorialEtapaToMatchUpdatableProperties(HistorialEtapa expectedHistorialEtapa) {
        assertHistorialEtapaAllUpdatablePropertiesEquals(expectedHistorialEtapa, getPersistedHistorialEtapa(expectedHistorialEtapa));
    }
}
