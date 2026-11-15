package co.edu.sgpg.web.rest;

import co.edu.sgpg.service.dto.ProyectoGradoDTO;
import co.edu.sgpg.service.dto.workflow.ComentarioWorkflowDTO;
import co.edu.sgpg.service.dto.workflow.RequisitosTransicionDTO;
import co.edu.sgpg.service.dto.workflow.SeguimientoProyectoDTO;
import co.edu.sgpg.service.workflow.MotorEtapasService;
import co.edu.sgpg.service.workflow.SeguimientoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/proyecto-grados/{id}")
public class ProyectoWorkflowResource {

    private final MotorEtapasService motor;
    private final SeguimientoService seguimiento;

    public ProyectoWorkflowResource(MotorEtapasService motor, SeguimientoService seguimiento) {
        this.motor = motor;
        this.seguimiento = seguimiento;
    }

    @GetMapping("/workflow")
    public RequisitosTransicionDTO requisitos(@PathVariable("id") Long id) {
        return motor.requisitos(id);
    }

    @PostMapping("/workflow/avanzar")
    public ProyectoGradoDTO avanzar(@PathVariable("id") Long id, @Valid @RequestBody(required = false) ComentarioWorkflowDTO body) {
        return motor.avanzar(id, body == null ? null : body.comentario());
    }

    @PostMapping("/workflow/cancelar")
    public ProyectoGradoDTO cancelar(@PathVariable("id") Long id, @Valid @RequestBody(required = false) ComentarioWorkflowDTO body) {
        return motor.cancelar(id, body == null ? null : body.comentario());
    }

    @GetMapping("/seguimiento")
    public SeguimientoProyectoDTO seguimiento(@PathVariable("id") Long id) {
        return seguimiento.obtener(id);
    }
}
