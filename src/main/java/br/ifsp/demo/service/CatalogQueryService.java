package br.ifsp.demo.service;

import br.ifsp.demo.controller.dto.RoomCatalogResponse;
import br.ifsp.demo.controller.dto.SportCatalogResponse;
import br.ifsp.demo.controller.dto.TrainerCatalogResponse;
import br.ifsp.demo.persistence.sqlite.SqliteCatalogQueries;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogQueryService {

    private final SqliteCatalogQueries queries;

    public CatalogQueryService(SqliteCatalogQueries queries) {
        this.queries = queries;
    }

    public List<SportCatalogResponse> findSports() {
        return queries.findSports().stream()
                .map(row -> new SportCatalogResponse(
                        row.id(), row.name(), row.description(), row.roomType(), row.active()))
                .toList();
    }

    public List<RoomCatalogResponse> findRooms() {
        return queries.findRooms().stream()
                .map(row -> new RoomCatalogResponse(
                        row.id(), row.name(), row.roomType(), row.capacity()))
                .toList();
    }

    public List<TrainerCatalogResponse> findTrainers() {
        return queries.findTrainers().stream()
                .map(row -> new TrainerCatalogResponse(row.id(), row.name(), row.active()))
                .toList();
    }
}
