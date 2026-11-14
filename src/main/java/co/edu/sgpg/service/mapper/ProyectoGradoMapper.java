package co.edu.sgpg.service.mapper;

import co.edu.sgpg.domain.ProyectoGrado;
import co.edu.sgpg.domain.User;
import co.edu.sgpg.service.dto.ProyectoGradoDTO;
import co.edu.sgpg.service.dto.UserDTO;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link ProyectoGrado} and its DTO {@link ProyectoGradoDTO}.
 */
@Mapper(componentModel = "spring")
public interface ProyectoGradoMapper extends EntityMapper<ProyectoGradoDTO, ProyectoGrado> {
    @Mapping(target = "tutor", source = "tutor", qualifiedByName = "userLogin")
    @Mapping(target = "estudiantes", source = "estudiantes", qualifiedByName = "userLoginSet")
    @Mapping(target = "jurados", source = "jurados", qualifiedByName = "userLoginSet")
    ProyectoGradoDTO toDto(ProyectoGrado s);

    @Mapping(target = "removeEstudiante", ignore = true)
    @Mapping(target = "removeJurado", ignore = true)
    ProyectoGrado toEntity(ProyectoGradoDTO proyectoGradoDTO);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);

    @Named("userLoginSet")
    default Set<UserDTO> toDtoUserLoginSet(Set<User> user) {
        return user.stream().map(this::toDtoUserLogin).collect(Collectors.toSet());
    }
}
