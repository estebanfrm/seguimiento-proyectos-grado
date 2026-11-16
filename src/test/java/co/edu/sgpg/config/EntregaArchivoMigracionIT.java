package co.edu.sgpg.config;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.sgpg.IntegrationTest;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.test.context.TestPropertySource;

/** Prueba la migración en un esquema propio, sin transacción de test ni datos de usuarios. */
@IntegrationTest
@TestPropertySource(properties = { "spring.liquibase.contexts=test", "application.liquibase.async-start=false" })
class EntregaArchivoMigracionIT {

    private static final byte[] PDF = "%PDF-1.7\n1 0 obj\n<<>>\nendobj\n%%EOF\n".getBytes(StandardCharsets.US_ASCII);

    @Autowired
    private DataSource dataSource;

    @ParameterizedTest
    @ValueSource(strings = { "oid", "bytea" })
    @Timeout(value = 2, unit = TimeUnit.MINUTES)
    void migracionConservaPdfAnteriorYNulosYAdmiteByteaExistente(String tipoInicial) throws Exception {
        String schema = "pdf_migracion_" + UUID.randomUUID().toString().replace("-", "");
        try (Connection connection = dataSource.getConnection()) {
            String schemaOriginal = connection.getSchema();
            connection.setAutoCommit(true);
            var jdbc = new JdbcTemplate(new SingleConnectionDataSource(connection, true));
            jdbc.execute("CREATE SCHEMA " + schema);
            Long oidCreado = null;
            try {
                connection.setSchema(schema);
                jdbc.execute("CREATE TABLE entrega (id bigint PRIMARY KEY, archivo " + tipoInicial + ")");
                if (tipoInicial.equals("oid")) {
                    oidCreado = jdbc.queryForObject("SELECT lo_from_bytea(0::oid, ?)", Long.class, PDF);
                    jdbc.update("INSERT INTO entrega VALUES (1, ?::oid)", oidCreado);
                } else {
                    jdbc.update("INSERT INTO entrega VALUES (1, ?)", PDF);
                }
                jdbc.update("INSERT INTO entrega VALUES (2, NULL)");
                var liquibase = new SpringLiquibase();
                liquibase.setResourceLoader(new DefaultResourceLoader());
                liquibase.setDataSource(new SingleConnectionDataSource(connection, true));
                liquibase.setDefaultSchema(schema);
                liquibase.setLiquibaseSchema(schema);
                liquibase.setChangeLog("classpath:config/liquibase/changelog/20261115010000_entrega_archivo_nombre.xml");
                liquibase.afterPropertiesSet();
                assertThat(jdbc.queryForObject("SELECT archivo FROM entrega WHERE id = 1", byte[].class)).isEqualTo(PDF);
                assertThat(jdbc.queryForObject("SELECT archivo IS NULL FROM entrega WHERE id = 2", Boolean.class)).isTrue();
                jdbc.update("UPDATE entrega SET archivo_nombre = ? WHERE id = 1", "Anterior.pdf");
                liquibase.afterPropertiesSet();
                assertThat(jdbc.queryForObject("SELECT archivo_nombre FROM entrega WHERE id = 1", String.class)).isEqualTo("Anterior.pdf");
            } finally {
                try {
                    connection.setSchema(schemaOriginal);
                } finally {
                    try {
                        if (oidCreado != null) jdbc.queryForObject("SELECT pg_catalog.lo_unlink(?::oid)", Integer.class, oidCreado);
                    } finally {
                        jdbc.execute("DROP SCHEMA " + schema + " CASCADE");
                    }
                }
            }
        }
    }
}
