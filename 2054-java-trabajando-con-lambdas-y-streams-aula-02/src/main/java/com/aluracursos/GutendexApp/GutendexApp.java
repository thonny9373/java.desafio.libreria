package com.aluracursos.GutendexApp;

import com.google.gson.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class GutendexApp {

	private static final String API_BASE_URL = "https://gutendex.com/books";
	private static final HttpClient httpClient = HttpClient.newHttpClient();
	private static final Gson gson = new Gson();

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		while (true) {
			printMenu();
			int option = getValidOption(scanner);

			switch (option) {
				case 1 -> searchBooksByTitle(scanner);
				case 2 -> listBooks();
				case 3 -> listAuthors();
				case 4 -> listAuthorsByYear(scanner);
				case 5 -> listBooksByLanguage(scanner);
				case 6 -> {
					System.out.println("Saliendo de la aplicación...");
					return;
				}
				default -> System.out.println("Opción no válida. Intente nuevamente.");
			}
		}
	}

	private static void printMenu() {
		System.out.println("\n--- Menú ---");
		System.out.println("1. Buscar libro por título");
		System.out.println("2. Listar libros registrados");
		System.out.println("3. Listar autores registrados");
		System.out.println("4. Listar autores vivos en un determinado año");
		System.out.println("5. Listar libros por idioma");
		System.out.println("6. Salir");
		System.out.print("Seleccione una opción: ");
	}

	private static int getValidOption(Scanner scanner) {
		while (!scanner.hasNextInt()) {
			System.out.println("Por favor, introduzca un número válido.");
			scanner.next(); // Consumir entrada no válida
		}
		int option = scanner.nextInt();
		scanner.nextLine(); // Consumir nueva línea sobrante
		return option;
	}

	private static void searchBooksByTitle(Scanner scanner) {
		System.out.print("Ingrese el título o parte del título: ");
		String title = scanner.nextLine();
		if (title.isBlank()) {
			System.out.println("El título no puede estar vacío.");
			return;
		}
		String url = API_BASE_URL + "?search=" + title.replace(" ", "%20");
		String response = makeRequest(url);
		if (response != null) {
			parseAndPrintBooks(response);
		}
	}

	private static void listBooks() {
		String response = makeRequest(API_BASE_URL);
		if (response != null) {
			parseAndPrintBooks(response);
		}
	}

	private static void listAuthors() {
		String response = makeRequest(API_BASE_URL);
		if (response != null) {
			Set<String> authors = parseAuthors(response);
			System.out.println("\nAutores registrados:");
			authors.forEach(System.out::println);
		}
	}

	private static void listAuthorsByYear(Scanner scanner) {
		System.out.print("Ingrese el año: ");
		while (!scanner.hasNextInt()) {
			System.out.println("Por favor, introduzca un número válido.");
			scanner.next(); // Consumir entrada no válida
		}
		int year = scanner.nextInt();
		scanner.nextLine(); // Consumir nueva línea sobrante
		String url = API_BASE_URL + "?author_year_start=" + year + "&author_year_end=" + year;
		String response = makeRequest(url);
		if (response != null) {
			Set<String> authors = parseAuthors(response);
			System.out.println("\nAutores vivos en el año " + year + ":");
			authors.forEach(System.out::println);
		}
	}

	private static void listBooksByLanguage(Scanner scanner) {
		System.out.print("Ingrese el código del idioma (ejemplo: en, fr, es): ");
		String language = scanner.nextLine().trim();
		if (language.isBlank()) {
			System.out.println("El código de idioma no puede estar vacío.");
			return;
		}
		String url = API_BASE_URL + "?languages=" + language;
		String response = makeRequest(url);
		if (response != null) {
			parseAndPrintBooks(response);
		}
	}

	private static String makeRequest(String url) {
		try {
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(url))
					.GET()
					.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() == 200) {
				return response.body();
			} else {
				System.out.println("Error en la solicitud. Código de estado: " + response.statusCode());
			}
		} catch (IOException | InterruptedException e) {
			System.out.println("Error al realizar la solicitud: " + e.getMessage());
		}
		return null;
	}

	private static void parseAndPrintBooks(String response) {
		try {
			JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
			JsonArray results = jsonObject.getAsJsonArray("results");

			if (results.size() == 0) {
				System.out.println("No se encontraron libros.");
				return;
			}

			System.out.println("\nLibros encontrados:");
			for (JsonElement element : results) {
				JsonObject book = element.getAsJsonObject();
				String title = book.get("title").getAsString();
				System.out.println("- " + title);
			}
		} catch (JsonSyntaxException e) {
			System.out.println("Error al procesar los datos del servidor.");
		}
	}

	private static Set<String> parseAuthors(String response) {
		Set<String> authors = new HashSet<>();
		try {
			JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
			JsonArray results = jsonObject.getAsJsonArray("results");

			for (JsonElement element : results) {
				JsonArray authorArray = element.getAsJsonObject().getAsJsonArray("authors");
				for (JsonElement authorElement : authorArray) {
					authors.add(authorElement.getAsJsonObject().get("name").getAsString());
				}
			}
		} catch (JsonSyntaxException e) {
			System.out.println("Error al procesar los datos del servidor.");
		}
		return authors;
	}
}
