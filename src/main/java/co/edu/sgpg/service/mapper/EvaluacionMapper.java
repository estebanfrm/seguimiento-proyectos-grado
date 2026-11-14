package co.edu.sgpg.service.mapper;

import co.edu.sgpg.domain.Evaluacion;
import co.edu.sgpg.domain.ProyectoGrado;
import co.edu.sgpg.domain.User;
import co.edu.sgpg.service.dto.EvaluacionDTO;
import co.edu.sgpg.service.dto.ProyectoGradoDTO;
import co.edu.sgpg.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Evaluacion} and its DTO {@link EvaluacionDTO}.
 */
@Mapper(componentModel = "spring")
public interface EvaluacionMapper extends EntityMapper<EvaluacionDTO, Evaluacion> {
    @Mapping(target = "jurado", source = "jurado", qualifiedByName = "userLogin")
    @Mapping(target = "proyecto", source = "proyecto", qualifiedByName = "proyectoGradoTitulo")
    EvaluacionDTO toDto(Evaluacion s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);

    @Named("proyectoGradoTitulo")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "titulo", source = "titulo")
    ProyectoGradoDTO toDtoProyectoGradoTitulo(ProyectoGrado proyectoGrado);
}
