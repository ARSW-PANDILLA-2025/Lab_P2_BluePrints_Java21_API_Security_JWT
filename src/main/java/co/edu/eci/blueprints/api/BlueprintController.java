package co.edu.eci.blueprints.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/blueprints")
@Tag(name = "Blueprints", description = "API for managing blueprints with JWT security")
@SecurityRequirement(name = "bearerAuth")
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
    @Operation(summary = "Get all blueprints", description = "Retrieves all blueprints (requires blueprints.read scope)")
    public ResponseEntity<List<Map<String, String>>> list() {
        List<Map<String, String>> blueprints = blueprintStorage.values()
                .stream()
                .toList();
        return ResponseEntity.ok(blueprints);
    }

    @GetMapping("/{author}")
    @PreAuthorize("hasAuthority('SCOPE_blueprints.read')")
    @Operation(summary = "Get blueprints by author", description = "Retrieves all blueprints by a specific author (requires blueprints.read scope)")
    public ResponseEntity<List<Map<String, String>>> getByAuthor(
            @Parameter(description = "Author name") @PathVariable String author) {
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
    @Operation(summary = "Get specific blueprint", description = "Retrieves a specific blueprint by author and name (requires blueprints.read scope)")
    public ResponseEntity<Map<String, String>> getByAuthorAndName(
            @Parameter(description = "Author name") @PathVariable String author,
            @Parameter(description = "Blueprint name") @PathVariable String name) {
        String key = author + "_" + name.replace(" ", "");
        Map<String, String> blueprint = blueprintStorage.get(key);

        if (blueprint == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(blueprint);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_blueprints.write')")
    @Operation(summary = "Create new blueprint", description = "Creates a new blueprint (requires blueprints.write scope)")
    public ResponseEntity<Map<String, String>> create(@RequestBody Map<String, String> blueprintData) {
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
    @Operation(summary = "Add point to blueprint", description = "Adds a point to an existing blueprint (requires blueprints.write scope)")
    public ResponseEntity<Map<String, String>> addPoint(
            @Parameter(description = "Author name") @PathVariable String author,
            @Parameter(description = "Blueprint name") @PathVariable String name,
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
    @Operation(summary = "Delete blueprint", description = "Deletes a specific blueprint (requires blueprints.write scope)")
    public ResponseEntity<Map<String, String>> delete(
            @Parameter(description = "Author name") @PathVariable String author,
            @Parameter(description = "Blueprint name") @PathVariable String name) {

        String key = author + "_" + name.replace(" ", "");
        Map<String, String> removed = blueprintStorage.remove(key);

        if (removed == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of("message", "Blueprint deleted successfully"));
    }
}
