package co.edu.sgpg.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import liquibase.changelog.ChangeLogParameters;
import liquibase.database.core.PostgresDatabase;
import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.util.csv.CSVReader;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** Comprueba los recursos de la demo sin iniciar Spring, Docker ni una base de datos. */
class DatosDemoTest {

    private static final String ROOT = "config/liquibase/";
    private static final String DEMO = ROOT + "changelog/20261113000000_datos_demo.xml";

    @Test
    void xmlCsvYColumnasCoincidenConElEsquemaGenerado() throws Exception {
        try (var resources = new ClassLoaderResourceAccessor()) {
            // El parser real valida también el XSD de Liquibase, sin conexiones de BD.
            var changelog = new XMLChangeLogSAXParser().parse(
                ROOT + "master.xml",
                new ChangeLogParameters(new PostgresDatabase()),
                resources
            );
            assertThat(changelog.getChangeSets()).anyMatch(change -> change.getId().equals("20261113000000-datos-demo"));
        }

        Map<String, Element> schemas = new HashMap<>();
        List<Element> constraints = new ArrayList<>();
        Document master = xml(ROOT + "master.xml");
        for (Element include : elements(master, "include")) {
            Document document = xml(include.getAttribute("file"));
            for (Element table : elements(document, "createTable")) {
                schemas.put(table.getAttribute("tableName"), table);
            }
            constraints.addAll(elements(document, "addForeignKeyConstraint"));
        }

        Map<String, List<Map<String, String>>> rowsByTable = new HashMap<>();
        for (Element load : elements(xml(DEMO), "loadData")) {
            String tableName = load.getAttribute("tableName");
            var csv = csv(ROOT + "data/demo/" + Path.of(load.getAttribute("file")).getFileName());
            var headers = new ArrayList<>(csv.getFirst().keySet());
            var schemaColumns = elements(schemas.get(tableName), "column");
            assertThat(headers).containsExactlyElementsOf(
                elements(load, "column")
                    .stream()
                    .map(c -> c.getAttribute("name"))
                    .toList()
            );
            assertThat(headers).isSubsetOf(
                schemaColumns
                    .stream()
                    .map(c -> c.getAttribute("name"))
                    .toList()
            );
            if (!tableName.equals("jhi_user")) {
                assertThat(headers).containsExactlyElementsOf(
                    schemaColumns
                        .stream()
                        .map(c -> c.getAttribute("name"))
                        .toList()
                );
            } else {
                assertThat(headers).containsExactlyElementsOf(
                    csv(ROOT + "data/user.csv")
                        .getFirst()
                        .keySet()
                );
            }
            for (Element column : schemaColumns) {
                String name = column.getAttribute("name");
                boolean required = elements(column, "constraints")
                    .stream()
                    .anyMatch(c -> c.getAttribute("nullable").equals("false"));
                for (var row : csv) {
                    if (required) {
                        assertThat(row.get(name))
                            .as(tableName + "." + name)
                            .isNotBlank()
                            .isNotEqualTo("NULL");
                    }
                    String type = column.getAttribute("type");
                    if (type.startsWith("varchar(") && row.containsKey(name)) {
                        int max = Integer.parseInt(type.substring(8, type.length() - 1));
                        assertThat(row.get(name).length())
                            .as(tableName + "." + name)
                            .isLessThanOrEqualTo(max);
                    }
                }
            }
            if (headers.contains("id")) {
                assertThat(
                    csv
                        .stream()
                        .map(row -> row.get("id"))
                        .toList()
                ).doesNotHaveDuplicates();
            } else {
                assertThat(csv).doesNotHaveDuplicates();
            }
            rowsByTable.put(tableName, csv);
        }
        rowsByTable.put("jhi_authority", csv(ROOT + "data/authority.csv"));
        for (Element fk : constraints) {
            String source = fk.getAttribute("baseTableName");
            for (var row : rowsByTable.getOrDefault(source, List.of())) {
                String value = row.get(fk.getAttribute("baseColumnNames"));
                if (value != null && !value.isEmpty() && !value.equals("NULL")) {
                    assertThat(rowsByTable.get(fk.getAttribute("referencedTableName")))
                        .as(fk.getAttribute("constraintName"))
                        .anyMatch(target -> value.equals(target.get(fk.getAttribute("referencedColumnNames"))));
                }
            }
        }
        assertThat(rowsByTable).hasSize(11);
    }

    @Test
    void usuariosDemoTienenLaContrasenaDocumentadaYDosAuthorities() throws Exception {
        var users = csv(ROOT + "data/demo/user.csv");
        var authorities = csv(ROOT + "data/demo/user_authority.csv");
        var encoder = new BCryptPasswordEncoder();
        Set<String> baseIds = new HashSet<>(
            csv(ROOT + "data/user.csv")
                .stream()
                .map(row -> row.get("id"))
                .toList()
        );
        assertThat(users).hasSize(11);
        assertThat(
            users
                .stream()
                .map(row -> row.get("login"))
                .toList()
        ).containsExactly(
            "coordinador",
            "estudiante1",
            "estudiante2",
            "estudiante3",
            "estudiante4",
            "estudiante5",
            "tutor1",
            "tutor2",
            "jurado1",
            "jurado2",
            "jurado3"
        );
        for (var user : users) {
            String login = user.get("login");
            String role = login.equals("coordinador")
                ? "ROLE_ADMIN"
                : login.startsWith("estudiante")
                  ? "ROLE_ESTUDIANTE"
                  : login.startsWith("tutor")
                    ? "ROLE_TUTOR"
                    : "ROLE_JURADO";
            assertThat(baseIds).doesNotContain(user.get("id"));
            assertThat(user.get("activated")).isEqualTo("true");
            assertThat(user.get("email")).isEqualTo(login + "@sgpg.example");
            assertThat(encoder.matches("user", user.get("password_hash")))
                .as(login)
                .isTrue();
            assertThat(
                authorities
                    .stream()
                    .filter(row -> row.get("user_id").equals(user.get("id")))
                    .map(row -> row.get("authority_name"))
                    .toList()
            ).containsExactlyInAnyOrder("ROLE_USER", role);
        }
    }

    @Test
    void demoSoloSeIncluyeEnDevYFakerNoSeActivaPorDefecto() throws Exception {
        var includes = elements(xml(ROOT + "master.xml"), "include");
        assertThat(includes.getLast().getAttribute("file")).isEqualTo(DEMO);
        assertThat(includes.getLast().getAttribute("context")).isEqualTo("dev");
        var change = elements(xml(DEMO), "changeSet").getFirst();
        assertThat(change.getAttribute("context")).isEqualTo("dev");
        assertThat(change.getAttribute("dbms")).isEqualTo("postgresql");
        String dev = Files.readString(Path.of("src/main/resources/config/application-dev.yml"));
        assertThat(dev).contains("contexts: dev").doesNotContain("contexts: dev, faker");
    }

    @Test
    void porcentajesDemoReservanElCienParaProyectosFinalizados() throws Exception {
        var proyectos = csv(ROOT + "data/demo/proyecto_grado.csv");
        assertThat(
            proyectos
                .stream()
                .map(proyecto -> proyecto.get("id"))
                .toList()
        ).containsExactly("2001", "2002", "2003", "2004", "2005", "2006");
        assertThat(
            proyectos
                .stream()
                .map(proyecto -> Integer.valueOf(proyecto.get("porcentaje_avance")))
                .toList()
        ).containsExactly(12, 16, 33, 48, 87, 100);
        for (var proyecto : proyectos) {
            int avance = Integer.parseInt(proyecto.get("porcentaje_avance"));
            if (proyecto.get("etapa_actual").equals("FINALIZADO")) {
                assertThat(avance).as(proyecto.get("titulo")).isEqualTo(100);
            } else {
                assertThat(avance).as(proyecto.get("titulo")).isLessThan(100);
            }
        }
    }

    private static Document xml(String resource) throws Exception {
        var factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        try (var stream = DatosDemoTest.class.getClassLoader().getResourceAsStream(resource)) {
            assertThat(stream).as(resource).isNotNull();
            return factory.newDocumentBuilder().parse(stream);
        }
    }

    private static List<Element> elements(org.w3c.dom.Node node, String name) {
        var nodes =
            node instanceof Document doc ? doc.getElementsByTagNameNS("*", name) : ((Element) node).getElementsByTagNameNS("*", name);
        return IntStream.range(0, nodes.getLength())
            .mapToObj(i -> (Element) nodes.item(i))
            .toList();
    }

    private static List<Map<String, String>> csv(String resource) throws Exception {
        try (
            var stream = DatosDemoTest.class.getClassLoader().getResourceAsStream(resource);
            var reader = new CSVReader(new InputStreamReader(stream, StandardCharsets.UTF_8), ';', '"')
        ) {
            String[] headers = reader.readNext();
            List<Map<String, String>> rows = new ArrayList<>();
            for (String[] values; (values = reader.readNext()) != null; ) {
                assertThat(values).as(resource).hasSameSizeAs(headers);
                Map<String, String> row = new LinkedHashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    row.put(headers[i], values[i]);
                }
                rows.add(row);
            }
            assertThat(rows).as(resource).isNotEmpty();
            return rows;
        }
    }
}
