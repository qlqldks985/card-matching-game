// 카드 짝 맞추기 - 클라이언트
// 카드의 그림은 서버만 알고 있다. 클라이언트는 카드를 뒤집을 때마다 서버에 물어본다.

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

async function api(path, body) {
    const res = await fetch(path, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body ?? {}),
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
        const data = await api('/api/games', { pairs: 8 });
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

function finish(r) {
    stopTimer();
    timerEl.textContent = `${r.elapsedSeconds}초`;
    resultTextEl.textContent = `${r.moves}번 시도, ${r.elapsedSeconds}초 만에 모든 짝을 맞췄어요!`;
    setTimeout(() => resultEl.classList.remove('hidden'), 500);
}

document.getElementById('restart').addEventListener('click', newGame);
document.getElementById('play-again').addEventListener('click', newGame);

newGame();
