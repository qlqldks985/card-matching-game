// 카드 짝 맞추기 - 클라이언트
// 카드의 그림은 서버만 알고 있다. 클라이언트는 카드를 뒤집을 때마다 서버에 물어본다.

// 틀린 카드를 보여 주는 시간. 서버의 봇 탐지(PlayTiming.MISMATCH_COOLDOWN)가 이 값을 기준으로 하므로 줄이지 말 것
const MISMATCH_DELAY_MS = 800;

const boardEl = document.getElementById('board');
const movesEl = document.getElementById('moves');
const pairsEl = document.getElementById('pairs');
const timerEl = document.getElementById('timer');
const messageEl = document.getElementById('message');
const resultEl = document.getElementById('result');
const resultTextEl = document.getElementById('result-text');

let gameId = null;
let busy = false;        // 서버 응답 대기 중이거나 틀린 카드를 보여주는 중
let timerHandle = null;
let startTime = null;
let pairs = loadPairs();

// 난이도 (짝 개수). 마지막으로 고른 난이도를 기억한다
const levelButtons = document.querySelectorAll('.level');

function loadPairs() {
    try {
        const saved = Number(localStorage.getItem('cardgame.pairs'));
        if ([6, 8, 12].includes(saved)) return saved;
    } catch (e) { /* 저장소를 못 쓰면 기본값 */ }
    return 8;
}

function selectLevel(value) {
    pairs = value;
    try { localStorage.setItem('cardgame.pairs', String(value)); } catch (e) { /* 무시 */ }
    levelButtons.forEach(b => {
        const on = Number(b.dataset.pairs) === value;
        b.classList.toggle('active', on);
        b.setAttribute('aria-checked', on);
    });
}

async function api(path, body) {
    const res = await fetch(path, body === undefined ? {} : {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
    });
    const data = await res.json().catch(() => ({}));
    if (!res.ok) {
        throw new Error(data.message || `요청 실패 (${res.status})`);
    }
    return data;
}

async function newGame() {
    messageEl.textContent = '';
    resultEl.classList.add('hidden');
    stopTimer();
    try {
        const data = await api('/api/games', { pairs });
        gameId = data.gameId;
        renderBoard(data.totalCards);
        updateStats({ moves: 0, matchedPairs: 0, totalPairs: data.totalCards / 2 });
        timerEl.textContent = '0초';
        startTime = null;
        busy = false;
    } catch (e) {
        messageEl.textContent = e.message;
    }
}

function renderBoard(totalCards) {
    boardEl.innerHTML = '';
    boardEl.style.setProperty('--cols', totalCards > 16 ? 6 : 4);
    for (let i = 0; i < totalCards; i++) {
        const card = document.createElement('button');
        card.type = 'button';
        card.className = 'card';
        card.dataset.index = i;
        card.setAttribute('aria-label', `카드 ${i + 1}`);
        card.innerHTML = '<span class="face back"></span><span class="face front"></span>';
        card.addEventListener('click', () => onCardClick(card));
        boardEl.appendChild(card);
    }
}

function cardAt(index) {
    return boardEl.querySelector(`.card[data-index="${index}"]`);
}

function show(card, symbol) {
    // 그림을 지우려고 예약해 둔 타이머가 남아 있으면 취소 (빠르게 다시 누른 경우)
    clearTimeout(card._clearTimer);
    card.querySelector('.front').textContent = symbol;
    card.classList.add('flipped');
}

function hide(card) {
    card.classList.remove('flipped');
    // 뒤집히는 애니메이션이 끝난 뒤 그림을 지워서 DOM에 정답이 남지 않게 한다
    card._clearTimer = setTimeout(() => { card.querySelector('.front').textContent = ''; }, 350);
}

async function onCardClick(card) {
    if (busy || card.classList.contains('flipped') || card.classList.contains('matched')) {
        return;
    }
    busy = true;
    messageEl.textContent = '';
    if (!startTime) startTimer();

    try {
        const r = await api(`/api/games/${gameId}/flip`, { index: Number(card.dataset.index) });
        show(card, r.symbol);
        updateStats(r);

        if (r.matched === null) {          // 첫 번째 카드
            busy = false;
            return;
        }

        const first = cardAt(r.pairedWith);
        if (r.matched) {
            first.classList.add('matched');
            card.classList.add('matched');
            busy = false;
            if (r.finished) finish(r);
        } else {
            setTimeout(() => {
                hide(first);
                hide(card);
                busy = false;
            }, MISMATCH_DELAY_MS);
        }
    } catch (e) {
        messageEl.textContent = e.message;
        busy = false;
    }
}

function updateStats({ moves, matchedPairs, totalPairs }) {
    movesEl.textContent = moves;
    pairsEl.textContent = `${matchedPairs} / ${totalPairs}`;
}

function startTimer() {
    startTime = Date.now();
    timerHandle = setInterval(() => {
        timerEl.textContent = `${Math.floor((Date.now() - startTime) / 1000)}초`;
    }, 250);
}

function stopTimer() {
    clearInterval(timerHandle);
    timerHandle = null;
}

function formatTime(ms) {
    return `${(ms / 1000).toFixed(1)}초`;
}

function finish(r) {
    stopTimer();
    timerEl.textContent = `${r.elapsedSeconds}초`;
    resultTextEl.textContent = `${r.moves}번 시도, ${formatTime(r.elapsedMillis)} 만에 모든 짝을 맞췄어요!`;
    recordForm.classList.remove('hidden');
    recordSubmit.disabled = false;
    recordMessage.textContent = '';
    recordMessage.classList.remove('ok');
    setTimeout(() => {
        resultEl.classList.remove('hidden');
        nicknameInput.focus();
    }, 500);
}

// ---- 기록 등록과 랭킹 ----

const LEVEL_NAMES = { 6: '쉬움', 8: '보통', 12: '어려움' };
const recordForm = document.getElementById('record-form');
const recordSubmit = document.getElementById('record-submit');
const recordMessage = document.getElementById('record-message');
const nicknameInput = document.getElementById('nickname');
const rankingBody = document.getElementById('ranking-body');
const rankingEmpty = document.getElementById('ranking-empty');
const rankingLevel = document.getElementById('ranking-level');
let myRecordId = null;  // 방금 등록한 기록을 랭킹에서 강조

try { nicknameInput.value = localStorage.getItem('cardgame.nickname') || ''; } catch (e) { /* 무시 */ }

recordForm.addEventListener('submit', async (event) => {
    event.preventDefault();
    const nickname = nicknameInput.value.trim();
    recordSubmit.disabled = true;
    recordMessage.classList.remove('ok');
    try {
        const r = await api(`/api/games/${gameId}/record`, { nickname });
        try { localStorage.setItem('cardgame.nickname', nickname); } catch (e) { /* 무시 */ }
        myRecordId = r.id;
        if (r.excluded) {
            // 서버가 봇 같은 플레이로 판단 (이유는 알려 주지 않는다)
            recordMessage.textContent = '비정상적인 플레이로 판단되어 랭킹에서 제외됐어요.';
        } else {
            recordMessage.textContent = `${LEVEL_NAMES[r.pairs] ?? r.pairs + '쌍'} ${r.rank}위로 등록됐어요!`;
            recordMessage.classList.add('ok');
        }
        loadRanking();
    } catch (e) {
        recordMessage.textContent = e.message;
        recordSubmit.disabled = false;
    }
});

let rankingRequest = 0;  // 난이도를 빠르게 바꿨을 때 늦게 온 이전 응답을 무시

async function loadRanking() {
    const request = ++rankingRequest;
    const level = pairs;
    rankingLevel.textContent = `· ${LEVEL_NAMES[level]} (${level}쌍)`;
    try {
        const list = await api(`/api/rankings?pairs=${level}&limit=10`);
        if (request !== rankingRequest) return;
        renderRanking(list);
    } catch (e) {
        if (request !== rankingRequest) return;
        rankingBody.innerHTML = '';
        rankingEmpty.textContent = `랭킹을 불러오지 못했어요: ${e.message}`;
        rankingEmpty.classList.remove('hidden');
    }
}

function renderRanking(list) {
    rankingBody.innerHTML = '';
    rankingEmpty.textContent = '아직 기록이 없어요. 첫 번째 주인공이 되어 보세요!';
    rankingEmpty.classList.toggle('hidden', list.length > 0);
    for (const entry of list) {
        const row = document.createElement('tr');
        if (entry.id === myRecordId) row.classList.add('mine');
        const medal = ['🥇', '🥈', '🥉'][entry.rank - 1] ?? entry.rank;
        for (const text of [medal, entry.nickname, `${entry.moves}번`, formatTime(entry.elapsedMillis)]) {
            const cell = document.createElement('td');
            cell.textContent = text;  // 닉네임을 HTML로 해석하지 않도록 textContent 사용
            row.appendChild(cell);
        }
        rankingBody.appendChild(row);
    }
}

levelButtons.forEach(b => b.addEventListener('click', () => {
    const value = Number(b.dataset.pairs);
    if (value === pairs) return;
    selectLevel(value);
    newGame();
    loadRanking();
}));

document.getElementById('restart').addEventListener('click', newGame);
document.getElementById('play-again').addEventListener('click', newGame);

selectLevel(pairs);
newGame();
loadRanking();
