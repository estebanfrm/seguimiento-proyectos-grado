package co.edu.sgpg.service.mapper;

import co.edu.sgpg.domain.HistorialEtapa;
import co.edu.sgpg.domain.ProyectoGrado;
import co.edu.sgpg.domain.User;
import co.edu.sgpg.service.dto.HistorialEtapaDTO;
import co.edu.sgpg.service.dto.ProyectoGradoDTO;
import co.edu.sgpg.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link HistorialEtapa} and its DTO {@link HistorialEtapaDTO}.
 */
@Mapper(componentModel = "spring")
public interface HistorialEtapaMapper extends EntityMapper<HistorialEtapaDTO, HistorialEtapa> {
    @Mapping(target = "usuario", source = "usuario", qualifiedByName = "userLogin")
    @Mapping(target = "proyecto", source = "proyecto", qualifiedByName = "proyectoGradoTitulo")
    HistorialEtapaDTO toDto(HistorialEtapa s);

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
