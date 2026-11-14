package co.edu.sgpg.service.mapper;

import co.edu.sgpg.domain.Entrega;
import co.edu.sgpg.domain.ProyectoGrado;
import co.edu.sgpg.domain.User;
import co.edu.sgpg.service.dto.EntregaDTO;
import co.edu.sgpg.service.dto.ProyectoGradoDTO;
import co.edu.sgpg.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Entrega} and its DTO {@link EntregaDTO}.
 */
@Mapper(componentModel = "spring")
public interface EntregaMapper extends EntityMapper<EntregaDTO, Entrega> {
    @Mapping(target = "autor", source = "autor", qualifiedByName = "userLogin")
    @Mapping(target = "proyecto", source = "proyecto", qualifiedByName = "proyectoGradoTitulo")
    EntregaDTO toDto(Entrega s);

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
