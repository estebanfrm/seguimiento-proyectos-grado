package co.edu.sgpg.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jackson.autoconfigure.JsonFactoryBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

@Configuration(proxyBeanMethods = false)
public class EntregaArchivoConfiguration {

    /** Deja pasar el base64 (incluido un margen sobre el límite) hasta la validación de negocio. */
    @Bean
    public JsonFactoryBuilderCustomizer limiteJsonEntregas(@Value("${sgpg.entregas.tamano-maximo-archivo:10MB}") DataSize tamanoMaximo) {
        long base64ConMargen = 4 * ((tamanoMaximo.toBytes() + 2) / 3) + DataSize.ofMegabytes(1).toBytes();
        return builder -> {
            var actuales = builder.build().streamReadConstraints();
            int longitud = (int) Math.min(Integer.MAX_VALUE, Math.max(actuales.getMaxStringLength(), base64ConMargen));
            builder.streamReadConstraints(actuales.rebuild().maxStringLength(longitud).build());
        };
    }
}
