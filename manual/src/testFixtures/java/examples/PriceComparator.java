package examples;

import static java.net.http.HttpClient.newHttpClient;
import static java.net.http.HttpRequest.newBuilder;
import static java.net.http.HttpResponse.BodyHandlers.ofString;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PriceComparator {

  private final List<Vendor> vendors;
  private final HttpClient httpClient = newHttpClient();

  public PriceComparator(Vendor... vendors) {
    this.vendors = Arrays.stream(vendors).toList();
  }

  public List<LookupResult> lookupPrice(String gtin) {
    return vendors.parallelStream()
        .map(
            vendor -> {
              try {
                HttpResponse<String> response =
                    httpClient.send(vendor.requestPriceForArticle(gtin), ofString());
                if (response.statusCode() != 200) {
                  return null;
                }
                return new LookupResult(vendor.name(), Integer.parseInt(response.body()));
              } catch (IOException | InterruptedException e) {
                return null;
              }
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  public interface Vendor {
    String name();

    HttpRequest requestPriceForArticle(String gtin);
  }

  public record CheeeperVendor(String apiUrl) implements Vendor {

    @Override
    public String name() {
      return "Cheeeper";
    }

    @Override
    public HttpRequest requestPriceForArticle(String gtin) {
      return newBuilder(URI.create(apiUrl).resolve("api/prices?id=%s".formatted(gtin)))
          .GET()
          .build();
    }
  }

  public record PrycyVendor(String apiUrl, String token) implements Vendor {

    @Override
    public String name() {
      return "Prycy";
    }

    @Override
    public HttpRequest requestPriceForArticle(String gtin) {
      return newBuilder(URI.create(apiUrl).resolve("api/price-requests/"))
          .POST(HttpRequest.BodyPublishers.ofString("{\"gtin\":\"%s\"}".formatted(gtin)))
          .header("Authorization", "Bearer %s".formatted(token))
          .build();
    }
  }

  public record LookupResult(String vendorName, int price) {}
}
