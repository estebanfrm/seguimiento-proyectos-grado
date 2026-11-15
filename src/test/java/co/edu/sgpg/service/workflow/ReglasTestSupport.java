package co.edu.sgpg.service.workflow;

import co.edu.sgpg.domain.Evaluacion;
import co.edu.sgpg.domain.ProyectoGrado;
import co.edu.sgpg.domain.User;
import co.edu.sgpg.domain.enumeration.Concepto;
import co.edu.sgpg.domain.enumeration.Etapa;
import co.edu.sgpg.repository.EntregaRepository;
import co.edu.sgpg.repository.EvaluacionRepository;
import co.edu.sgpg.repository.FechaLimiteEtapaRepository;
import co.edu.sgpg.repository.ObservacionRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
abstract class ReglasTestSupport {

    @Mock
    protected EntregaRepository entregas;

    @Mock
    protected ObservacionRepository observaciones;

    @Mock
    protected EvaluacionRepository evaluaciones;

    @Mock
    protected FechaLimiteEtapaRepository fechas;

    protected Requisitos requisitos;

    @BeforeEach
    void crearRequisitos() {
        requisitos = new Requisitos(entregas, observaciones, evaluaciones, fechas);
    }

    protected ProyectoGrado proyecto(Etapa etapa) {
        return new ProyectoGrado().id(1L).etapaActual(etapa);
    }

    protected User usuario(long id) {
        User usuario = new User();
        usuario.setId(id);
        return usuario;
    }

    protected Evaluacion nota(User jurado, String calificacion, Concepto concepto) {
        return new Evaluacion().jurado(jurado).calificacion(new BigDecimal(calificacion)).concepto(concepto);
    }
}
