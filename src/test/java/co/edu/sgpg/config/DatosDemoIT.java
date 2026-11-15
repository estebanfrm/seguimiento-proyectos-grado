package co.edu.sgpg.config;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.sgpg.IntegrationTest;
import java.sql.Connection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.test.context.TestPropertySource;

/** Carga la demo real de desarrollo sin contaminar las tablas compartidas de otros IT. */
@IntegrationTest
@TestPropertySource(properties = { "spring.liquibase.contexts=test", "application.liquibase.async-start=false" })
class DatosDemoIT {

    @Autowired
    private DataSource dataSource;

    @Test
    @Timeout(value = 2, unit = TimeUnit.MINUTES)
    void cargaDevConUsuariosSeisEscenariosYSecuenciaSegura() throws Exception {
        String schema = "datos_demo_" + UUID.randomUUID().toString().replace("-", "");
        try (Connection connection = dataSource.getConnection()) {
            String originalSchema = connection.getSchema();
            connection.setAutoCommit(true);
            var jdbc = new JdbcTemplate(new SingleConnectionDataSource(connection, true));
            jdbc.execute("CREATE SCHEMA " + schema);
            try {
                connection.setSchema(schema);
                var liquibase = new SpringLiquibase();
                liquibase.setResourceLoader(new DefaultResourceLoader());
                liquibase.setDataSource(new SingleConnectionDataSource(connection, true));
                liquibase.setChangeLog("classpath:config/liquibase/master.xml");
                liquibase.setContexts("dev");
                liquibase.setDefaultSchema(schema);
                liquibase.setLiquibaseSchema(schema);
                liquibase.afterPropertiesSet();

                assertThat(jdbc.queryForObject("SELECT count(*) FROM jhi_user WHERE email LIKE '%@sgpg.example'", Long.class)).isEqualTo(
                    11
                );
                assertThat(
                    jdbc.queryForObject("SELECT count(*) FROM jhi_user_authority WHERE user_id BETWEEN 1001 AND 1011", Long.class)
                ).isEqualTo(22);
                assertThat(jdbc.queryForObject("SELECT count(*) FROM proyecto_grado", Long.class)).isEqualTo(6);
                assertThat(jdbc.queryForList("SELECT porcentaje_avance FROM proyecto_grado ORDER BY id", Integer.class)).containsExactly(
                    12,
                    16,
                    33,
                    48,
                    87,
                    100
                );
                assertThat(jdbc.queryForList("SELECT etapa_actual FROM proyecto_grado ORDER BY id", String.class)).containsExactly(
                    "PROPUESTA",
                    "PROPUESTA",
                    "ANTEPROYECTO",
                    "DESARROLLO",
                    "SUSTENTACION",
                    "FINALIZADO"
                );
                assertThat(jdbc.queryForObject("SELECT estado FROM entrega WHERE id=3001", String.class)).isEqualTo("ENVIADA");
                assertThat(jdbc.queryForObject("SELECT estado FROM entrega WHERE id=3002", String.class)).isEqualTo("APROBADA");
                assertThat(jdbc.queryForObject("SELECT count(*) FROM observacion WHERE entrega_id=3002", Long.class)).isZero();
                assertThat(
                    jdbc.queryForObject("SELECT count(*) FROM rel_proyecto_grado__jurado WHERE proyecto_grado_id=2003", Long.class)
                ).isEqualTo(2);
                assertThat(
                    jdbc.queryForObject("SELECT count(*) FROM evaluacion WHERE proyecto_id=2003 AND etapa='ANTEPROYECTO'", Long.class)
                ).isEqualTo(1);
                assertThat(jdbc.queryForObject("SELECT estado FROM entrega WHERE id=3007", String.class)).isEqualTo("CON_OBSERVACIONES");
                assertThat(
                    jdbc.queryForObject("SELECT resuelta FROM observacion WHERE id=4001 AND tipo='CORRECCION_REQUERIDA'", Boolean.class)
                ).isFalse();
                assertThat(
                    jdbc.queryForObject(
                        "SELECT count(*) FROM fecha_limite_etapa WHERE fecha_limite < TIMESTAMP '2026-11-13 00:00:00'",
                        Long.class
                    )
                ).isEqualTo(1);
                assertThat(
                    jdbc.queryForObject(
                        "SELECT count(*) FROM fecha_limite_etapa WHERE proyecto_id=2004 AND etapa='DESARROLLO' AND fecha_limite=TIMESTAMP '2026-11-01 23:59:00'",
                        Long.class
                    )
                ).isEqualTo(1);
                assertThat(
                    jdbc.queryForObject(
                        "SELECT count(*) FROM fecha_limite_etapa WHERE fecha_limite >= TIMESTAMP '2027-01-01 00:00:00' AND fecha_limite < TIMESTAMP '2028-01-01 00:00:00'",
                        Long.class
                    )
                ).isEqualTo(23);
                assertThat(
                    jdbc.queryForObject(
                        "SELECT count(*) FROM evaluacion WHERE proyecto_id=2005 AND etapa='SUSTENTACION' AND calificacion>=3 AND concepto='APROBADO'",
                        Long.class
                    )
                ).isEqualTo(2);
                assertThat(
                    jdbc.queryForObject(
                        "SELECT count(*) FROM fecha_limite_etapa WHERE proyecto_id=2005 AND etapa='SUSTENTACION'",
                        Long.class
                    )
                ).isEqualTo(1);
                assertThat(jdbc.queryForObject("SELECT estado FROM proyecto_grado WHERE id=2006", String.class)).isEqualTo("APROBADO");
                assertThat(
                    jdbc.queryForList("SELECT etapa_destino FROM historial_etapa WHERE proyecto_id=2006 ORDER BY fecha", String.class)
                ).containsExactly("ANTEPROYECTO", "DESARROLLO", "SUSTENTACION", "FINALIZADO");
                assertThat(jdbc.queryForObject("SELECT count(*) FROM databasechangelog WHERE id LIKE '%-data'", Long.class)).isZero();
                long nextId = jdbc.queryForObject("SELECT nextval('sequence_generator')", Long.class);
                assertThat(nextId - 49).isGreaterThan(7010);

                // Una segunda ejecución no vuelve a insertar filas ni reduce la secuencia.
                liquibase.afterPropertiesSet();
                assertThat(jdbc.queryForObject("SELECT count(*) FROM proyecto_grado", Long.class)).isEqualTo(6);
                assertThat(jdbc.queryForObject("SELECT last_value FROM sequence_generator", Long.class)).isEqualTo(nextId);
            } finally {
                connection.setSchema(originalSchema);
                jdbc.execute("DROP SCHEMA " + schema + " CASCADE");
            }
        }
    }
}
