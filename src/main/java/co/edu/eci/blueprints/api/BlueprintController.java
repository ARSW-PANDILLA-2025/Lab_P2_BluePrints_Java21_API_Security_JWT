package co.edu.eci.blueprints.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/blueprints")
@Tag(name = "Blueprints", description = "API for managing blueprints with JWT security. Requires authentication with valid JWT token containing appropriate scopes.")
@SecurityRequirement(name = "bearer-jwt")
public class BlueprintController {

    private final Map<String, Map<String, String>> blueprintStorage = new ConcurrentHashMap<>();

    public BlueprintController() {
        blueprintStorage.put("student_b1", Map.of(
                "id", "b1",
                "name", "Casa de campo",
                "author", "student",
                "points", "[(0,0), (10,10), (20,0)]"));
        blueprintStorage.put("student_b2", Map.of(
                "id", "b2",
                "name", "Edificio urbano",
                "author", "student",
                "points", "[(0,0), (5,15), (10,0), (15,10)]"));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_blueprints.read')")
    @Operation(
        summary = "Get all blueprints",
        description = "Retrieves all blueprints available in the system. Requires 'blueprints.read' scope in JWT token."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "List of blueprints returned successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = "[{\"id\":\"b1\",\"name\":\"Casa de campo\",\"author\":\"student\",\"points\":\"[(0,0), (10,10), (20,0)]\"}]")
            )
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden - Missing 'blueprints.read' scope", content = @Content)
    })
    public ResponseEntity<List<Map<String, String>>> list() {
        List<Map<String, String>> blueprints = blueprintStorage.values()
                .stream()
                .toList();
        return ResponseEntity.ok(blueprints);
    }

    @GetMapping("/{author}")
    @PreAuthorize("hasAuthority('SCOPE_blueprints.read')")
    @Operation(
        summary = "Get blueprints by author",
        description = "Retrieves all blueprints created by a specific author. Requires 'blueprints.read' scope in JWT token."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "List of blueprints by author returned successfully",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(responseCode = "404", description = "No blueprints found for the specified author", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden - Missing 'blueprints.read' scope", content = @Content)
    })
    public ResponseEntity<List<Map<String, String>>> getByAuthor(
            @Parameter(description = "Author name", example = "student", required = true)
            @PathVariable String author) {
        List<Map<String, String>> authorBlueprints = blueprintStorage.values()
                .stream()
                .filter(bp -> author.equals(bp.get("author")))
                .toList();

        if (authorBlueprints.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(authorBlueprints);
    }

    @GetMapping("/{author}/{name}")
    @PreAuthorize("hasAuthority('SCOPE_blueprints.read')")
    @Operation(
        summary = "Get specific blueprint",
        description = "Retrieves a specific blueprint by author and name. Requires 'blueprints.read' scope in JWT token."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Blueprint found and returned successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = "{\"id\":\"b1\",\"name\":\"Casa de campo\",\"author\":\"student\",\"points\":\"[(0,0), (10,10), (20,0)]\"}")
            )
        ),
        @ApiResponse(responseCode = "404", description = "Blueprint not found", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden - Missing 'blueprints.read' scope", content = @Content)
    })
    public ResponseEntity<Map<String, String>> getByAuthorAndName(
            @Parameter(description = "Author name", example = "student", required = true)
            @PathVariable String author,
            @Parameter(description = "Blueprint name (spaces will be removed)", example = "Casa de campo", required = true)
            @PathVariable String name) {
        String key = author + "_" + name.replace(" ", "");
        Map<String, String> blueprint = blueprintStorage.get(key);

        if (blueprint == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(blueprint);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_blueprints.write')")
    @Operation(
        summary = "Create new blueprint",
        description = "Creates a new blueprint in the system. Requires 'blueprints.write' scope in JWT token."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Blueprint created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = "{\"id\":\"bp_1695177600000\",\"name\":\"Mi Plano\",\"author\":\"student\",\"points\":\"[]\"}")
            )
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden - Missing 'blueprints.write' scope", content = @Content)
    })
    public ResponseEntity<Map<String, String>> create(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Blueprint data to create",
            required = true,
            content = @Content(
                schema = @Schema(
                    example = "{\"name\":\"Mi Plano\",\"author\":\"student\",\"points\":\"[(0,0), (5,5)]\"}"
                )
            )
        )
        @RequestBody Map<String, String> blueprintData) {

        String name = blueprintData.getOrDefault("name", "nuevo");
        String author = blueprintData.getOrDefault("author", "unknown");
        String points = blueprintData.getOrDefault("points", "[]");

        String id = "bp_" + System.currentTimeMillis();
        String key = author + "_" + name.replace(" ", "");

        Map<String, String> newBlueprint = Map.of(
                "id", id,
                "name", name,
                "author", author,
                "points", points);

        blueprintStorage.put(key, newBlueprint);
        return ResponseEntity.status(HttpStatus.CREATED).body(newBlueprint);
    }

    @PutMapping("/{author}/{name}/points")
    @PreAuthorize("hasAuthority('SCOPE_blueprints.write')")
    @Operation(
        summary = "Add point to blueprint",
        description = "Adds a new point to an existing blueprint. Requires 'blueprints.write' scope in JWT token."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "202",
            description = "Point added successfully to blueprint",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = "{\"message\":\"Point added successfully\",\"point\":\"(10,20)\"}")
            )
        ),
        @ApiResponse(responseCode = "404", description = "Blueprint not found", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden - Missing 'blueprints.write' scope", content = @Content)
    })
    public ResponseEntity<Map<String, String>> addPoint(
            @Parameter(description = "Author name", example = "student", required = true)
            @PathVariable String author,
            @Parameter(description = "Blueprint name", example = "Casa de campo", required = true)
            @PathVariable String name,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Point coordinates to add",
                required = true,
                content = @Content(
                    schema = @Schema(example = "{\"x\":10,\"y\":20}")
                )
            )
            @RequestBody Map<String, Object> pointData) {

        String key = author + "_" + name.replace(" ", "");
        Map<String, String> blueprint = blueprintStorage.get(key);

        if (blueprint == null) {
            return ResponseEntity.notFound().build();
        }

        String currentPoints = blueprint.get("points");
        String newPoint = String.format("(%s,%s)",
                pointData.get("x"), pointData.get("y"));

        Map<String, String> updatedBlueprint = new ConcurrentHashMap<>(blueprint);
        updatedBlueprint.put("points", currentPoints + ", " + newPoint);
        blueprintStorage.put(key, updatedBlueprint);

        return ResponseEntity.accepted().body(Map.of(
                "message", "Point added successfully",
                "point", newPoint));
    }

    @DeleteMapping("/{author}/{name}")
    @PreAuthorize("hasAuthority('SCOPE_blueprints.write')")
    @Operation(
        summary = "Delete blueprint",
        description = "Deletes a specific blueprint from the system. Requires 'blueprints.write' scope in JWT token."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Blueprint deleted successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = "{\"message\":\"Blueprint deleted successfully\"}")
            )
        ),
        @ApiResponse(responseCode = "404", description = "Blueprint not found", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden - Missing 'blueprints.write' scope", content = @Content)
    })
    public ResponseEntity<Map<String, String>> delete(
            @Parameter(description = "Author name", example = "student", required = true)
            @PathVariable String author,
            @Parameter(description = "Blueprint name", example = "Casa de campo", required = true)
            @PathVariable String name) {

        String key = author + "_" + name.replace(" ", "");
        Map<String, String> removed = blueprintStorage.remove(key);

        if (removed == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of("message", "Blueprint deleted successfully"));
    }
}
