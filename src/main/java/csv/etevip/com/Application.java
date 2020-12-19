package csv.etevip.com;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;

import org.apache.commons.io.IOUtils;

public class Application {

    private static HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static String SERVER_URL = "https://run.mocky.io/v3/8d7935df-c3e5-4911-9580-df222471a2ef";

    public static void main(String[] args) throws IOException, InterruptedException {

        var mustache = loadBodyJSONTemplate();
        var orders = LoadCSV(Paths.get("/home/renato/Documents/repositories/java-lang/csv-etevip/files/template.csv"));

        while (orders.hasNext()) {
            var writer = mustache.execute(new StringWriter(), orders.next());

            System.out.println("try Send json %s \n".formatted(writer));
            var response = sendPostRequest(writer.toString());

            System.out.println("return status=%s\n body=%s\n".formatted(response.statusCode(), response.body()) );
            writer.flush();
        }
    }

    public static Mustache loadBodyJSONTemplate() throws IOException {
        var body = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("templates/body.mustache"),
                StandardCharsets.UTF_8);
        var factory = new DefaultMustacheFactory();
        var mustache = factory.compile(new StringReader(body), "json-body");
        return mustache;
    }

    public static Iterator<Map<String, Object>> LoadCSV(Path csv) throws IOException {
        CsvSchema schema = CsvSchema.emptySchema().withHeader();

        CsvMapper csvMapper = new CsvMapper();
        MappingIterator<Map<String, Object>> orderLines = csvMapper.readerFor(Map.class).with(schema)
                .readValues(csv.toFile());

        return orderLines;
    }

    public static HttpResponse<String> sendPostRequest(String body) throws IOException, InterruptedException {

        var request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_URL))
                .header("Content-Type", "application/json").POST(BodyPublishers.ofString(body)).build();

        return HTTP_CLIENT.send(request, BodyHandlers.ofString());
    }
}
