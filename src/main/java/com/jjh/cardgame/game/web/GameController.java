package com.jjh.cardgame.game.web;

import com.jjh.cardgame.game.domain.Game;
import com.jjh.cardgame.game.dto.CreateGameRequest;
import com.jjh.cardgame.game.dto.CreateGameResponse;
import com.jjh.cardgame.game.dto.FlipRequest;
import com.jjh.cardgame.game.dto.FlipResponse;
import com.jjh.cardgame.game.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    /** 새 게임 시작 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateGameResponse create(@Valid @RequestBody(required = false) CreateGameRequest request) {
        Game game = gameService.create(request != null ? request.pairs() : null);
        return new CreateGameResponse(game.getId(), game.totalCards());
    }

    /** 카드 한 장 뒤집기 */
    @PostMapping("/{gameId}/flip")
    public FlipResponse flip(@PathVariable String gameId, @Valid @RequestBody FlipRequest request) {
        return gameService.flip(gameId, request.index());
    }
}
