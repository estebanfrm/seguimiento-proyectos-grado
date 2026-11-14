package co.edu.sgpg.service.mapper;

import co.edu.sgpg.domain.Entrega;
import co.edu.sgpg.domain.Observacion;
import co.edu.sgpg.domain.User;
import co.edu.sgpg.service.dto.EntregaDTO;
import co.edu.sgpg.service.dto.ObservacionDTO;
import co.edu.sgpg.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Observacion} and its DTO {@link ObservacionDTO}.
 */
@Mapper(componentModel = "spring")
public interface ObservacionMapper extends EntityMapper<ObservacionDTO, Observacion> {
    @Mapping(target = "autor", source = "autor", qualifiedByName = "userLogin")
    @Mapping(target = "entrega", source = "entrega", qualifiedByName = "entregaTitulo")
    ObservacionDTO toDto(Observacion s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);

    @Named("entregaTitulo")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "titulo", source = "titulo")
    EntregaDTO toDtoEntregaTitulo(Entrega entrega);
}
