package com.jjh.cardgame.ranking;

import com.jayway.jsonpath.JsonPath;
import com.jjh.cardgame.game.domain.Game;
import com.jjh.cardgame.game.service.GameService;
import com.jjh.cardgame.ranking.domain.GameRecordRepository;
import com.jjh.cardgame.support.GameSolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RankingApiTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    GameService gameService;

    @Autowired
    GameRecordRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    private String newGame(int pairs) throws Exception {
        String body = mvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pairs\":" + pairs + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.gameId");
    }

    private String finishedGame(int pairs) throws Exception {
        String id = newGame(pairs);
        GameSolver.solve(gameService.find(id));
        return id;
    }

    private org.springframework.test.web.servlet.ResultActions submit(String gameId, String nickname) throws Exception {
        return mvc.perform(post("/api/games/{id}/record", gameId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nickname\":\"" + nickname + "\"}"));
    }

    @Test
    void 끝난_게임을_등록하면_서버가_판정한_기록과_순위를_돌려준다() throws Exception {
        String id = finishedGame(4);
        Game game = gameService.find(id);

        submit(id, " 여우_01 ")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rank").value(1))
                .andExpect(jsonPath("$.nickname").value("여우_01"))
                .andExpect(jsonPath("$.pairs").value(4))
                .andExpect(jsonPath("$.moves").value(game.getMoves()));

        mvc.perform(get("/api/rankings").param("pairs", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nickname").value("여우_01"));
    }

    @Test
    void 같은_게임은_두_번_등록할_수_없다() throws Exception {
        String id = finishedGame(2);
        submit(id, "first").andExpect(status().isCreated());
        submit(id, "again").andExpect(status().isConflict());
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void 끝나지_않은_게임은_등록할_수_없다() throws Exception {
        String id = newGame(4);
        submit(id, "cheater")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("아직 끝나지 않은 게임입니다."));
    }

    @Test
    void 없는_게임은_404() throws Exception {
        submit("no-such-game", "nobody").andExpect(status().isNotFound());
    }

    @Test
    void 닉네임_규칙을_어기면_400() throws Exception {
        String id = finishedGame(2);
        submit(id, "").andExpect(status().isBadRequest());
        submit(id, "열세글자가넘는아주긴닉네임").andExpect(status().isBadRequest());
        submit(id, "<script>").andExpect(status().isBadRequest());
        assertThat(repository.count()).isZero();
    }

    @Test
    void 랭킹은_난이도별로_나뉜다() throws Exception {
        submit(finishedGame(2), "easy").andExpect(status().isCreated());
        submit(finishedGame(3), "other").andExpect(status().isCreated());

        mvc.perform(get("/api/rankings").param("pairs", "2"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nickname").value("easy"));
    }

    @Test
    void limit_범위를_벗어나면_400() throws Exception {
        mvc.perform(get("/api/rankings").param("limit", "0")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/rankings").param("limit", "51")).andExpect(status().isBadRequest());
    }
}
