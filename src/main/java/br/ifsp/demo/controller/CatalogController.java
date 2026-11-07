package br.ifsp.demo.controller;

import br.ifsp.demo.controller.dto.RoomCatalogResponse;
import br.ifsp.demo.controller.dto.SportCatalogResponse;
import br.ifsp.demo.controller.dto.TrainerCatalogResponse;
import br.ifsp.demo.service.CatalogQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Catalogs", description = "Read-only catalogs used by the administrative activity forms.")
@SecurityRequirement(name = "bearerAuth")
public class CatalogController {

    private final CatalogQueryService catalogQueryService;

    public CatalogController(CatalogQueryService catalogQueryService) {
        this.catalogQueryService = catalogQueryService;
    }

    @GetMapping("/sports")
    @Operation(summary = "List sports")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sports returned.",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = SportCatalogResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Authentication is required."),
            @ApiResponse(responseCode = "403", description = "Only administrators can access catalogs.")
    })
    public List<SportCatalogResponse> findSports() {
        return catalogQueryService.findSports();
    }

    @GetMapping("/rooms")
    @Operation(summary = "List rooms")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rooms returned.",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = RoomCatalogResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Authentication is required."),
            @ApiResponse(responseCode = "403", description = "Only administrators can access catalogs.")
    })
    public List<RoomCatalogResponse> findRooms() {
        return catalogQueryService.findRooms();
    }

    @GetMapping("/trainers")
    @Operation(summary = "List trainers")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainers returned.",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TrainerCatalogResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Authentication is required."),
            @ApiResponse(responseCode = "403", description = "Only administrators can access catalogs.")
    })
    public List<TrainerCatalogResponse> findTrainers() {
        return catalogQueryService.findTrainers();
    }
}
