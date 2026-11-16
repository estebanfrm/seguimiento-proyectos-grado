package co.edu.sgpg.service.mapper;

import co.edu.sgpg.domain.FechaLimiteEtapa;
import co.edu.sgpg.domain.ProyectoGrado;
import co.edu.sgpg.service.dto.FechaLimiteEtapaDTO;
import co.edu.sgpg.service.dto.ProyectoGradoDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link FechaLimiteEtapa} and its DTO {@link FechaLimiteEtapaDTO}.
 */
@Mapper(componentModel = "spring")
public interface FechaLimiteEtapaMapper extends EntityMapper<FechaLimiteEtapaDTO, FechaLimiteEtapa> {
    @Mapping(target = "proyecto", source = "proyecto", qualifiedByName = "proyectoGradoTitulo")
    FechaLimiteEtapaDTO toDto(FechaLimiteEtapa s);

    @Named("proyectoGradoTitulo")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "titulo", source = "titulo")
    ProyectoGradoDTO toDtoProyectoGradoTitulo(ProyectoGrado proyectoGrado);
}
