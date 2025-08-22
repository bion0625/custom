package org.example;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MainApp extends Application {

    // ----- 가상 해상도(픽셀 아트 해상도) & 스케일 -----
    private static final int TILE = 16;          // 타일 크기 (픽셀 단위)
    private static final int VIEW_W = 20 * TILE; // 가상 뷰포트 가로 (타일 20개)
    private static final int VIEW_H = 15 * TILE; // 가상 뷰포트 세로 (타일 15개)
    private static final int SCALE  = 4;         // 실제 화면 배율 (도트 확대)
    private static final int SCREEN_W = VIEW_W * SCALE;
    private static final int SCREEN_H = VIEW_H * SCALE;

    // ----- 맵 -----
    private int MAP_W = 40; // 타일 수
    private int MAP_H = 30;
    private int[][] map;    // 0=잔디, 1=벽

    // ----- 플레이어 -----
    private double px = 5 * TILE, py = 5 * TILE; // 플레이어 위치(픽셀)
    private double pw = 12, ph = 14;             // 충돌 박스 크기(픽셀)
    private double speed = 2.0;                  // 이동 속도 (픽셀/프레임)
    private int animTick = 0;                    // 발걸음 애니메이션용

    // 입력
    private boolean up, down, left, right;

    // 카메라
    private double camX = 0, camY = 0;

    // 렌더링 캔버스
    private Canvas canvas;
    private GraphicsContext gc;

    @Override
    public void start(Stage stage) {
        canvas = new Canvas(SCREEN_W, SCREEN_H);
        gc = canvas.getGraphicsContext2D();
        gc.setImageSmoothing(false); // 이미지 보간(블러) 방지

        // 맵 생성
        genMap();

        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root, SCREEN_W, SCREEN_H);

        // 키 입력
        scene.setOnKeyPressed(e -> {
            KeyCode c = e.getCode();
            if (c == KeyCode.UP || c == KeyCode.W) up = true;
            if (c == KeyCode.DOWN || c == KeyCode.S) down = true;
            if (c == KeyCode.LEFT || c == KeyCode.A) left = true;
            if (c == KeyCode.RIGHT || c == KeyCode.D) right = true;
        });
        scene.setOnKeyReleased(e -> {
            KeyCode c = e.getCode();
            if (c == KeyCode.UP || c == KeyCode.W) up = false;
            if (c == KeyCode.DOWN || c == KeyCode.S) down = false;
            if (c == KeyCode.LEFT || c == KeyCode.A) left = false;
            if (c == KeyCode.RIGHT || c == KeyCode.D) right = false;
        });

        stage.setScene(scene);
        stage.setTitle("JavaFX Pixel RPG (prototype)");
        stage.show();
        canvas.requestFocus();

        // 게임 루프
        new AnimationTimer() {
            @Override public void handle(long now) {
                update();
                render();
            }
        }.start();
    }

    private void genMap() {
        map = new int[MAP_H][MAP_W];
        // 테두리 벽
        for (int y = 0; y < MAP_H; y++) {
            for (int x = 0; x < MAP_W; x++) {
                if (x == 0 || y == 0 || x == MAP_W - 1 || y == MAP_H - 1) {
                    map[y][x] = 1; // 벽
                } else {
                    map[y][x] = 0; // 잔디
                }
            }
        }
        // 내부 장애물 몇 개
        for (int x = 8; x < 14; x++) map[10][x] = 1;
        for (int y = 6; y < 12; y++) map[y][20] = 1;
        for (int x = 24; x < 30; x++) map[18][x] = 1;
    }

    private void update() {
        animTick++;

        double dx = 0, dy = 0;
        if (up) dy -= speed;
        if (down) dy += speed;
        if (left) dx -= speed;
        if (right) dx += speed;

        // 대각선 보정
        if (dx != 0 && dy != 0) {
            dx *= 0.7071;
            dy *= 0.7071;
        }

        // 충돌 포함 이동
        move(dx, dy);

        // 카메라 위치 (플레이어 중심)
        camX = clamp(px + pw/2 - VIEW_W / 2.0, 0, MAP_W * TILE - VIEW_W);
        camY = clamp(py + ph/2 - VIEW_H / 2.0, 0, MAP_H * TILE - VIEW_H);
    }

    private void move(double dx, double dy) {
        // X축
        if (dx != 0) {
            double nx = px + dx;
            if (!collides(nx, py)) px = nx;
        }
        // Y축
        if (dy != 0) {
            double ny = py + dy;
            if (!collides(px, ny)) py = ny;
        }
    }

    private boolean collides(double x, double y) {
        // 플레이어 충돌 박스 기준 4코너 검사
        int left = (int)Math.floor(x / TILE);
        int right = (int)Math.floor((x + pw - 1) / TILE);
        int top = (int)Math.floor(y / TILE);
        int bottom = (int)Math.floor((y + ph - 1) / TILE);

        for (int ty = top; ty <= bottom; ty++) {
            for (int tx = left; tx <= right; tx++) {
                if (isSolid(tx, ty)) return true;
            }
        }
        return false;
    }

    private boolean isSolid(int tx, int ty) {
        if (tx < 0 || ty < 0 || tx >= MAP_W || ty >= MAP_H) return true;
        return map[ty][tx] == 1;
    }

    private void render() {
        // 배경(검정)
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, SCREEN_W, SCREEN_H);

        // 스케일과 카메라 변환 적용
        gc.save();
        gc.scale(SCALE, SCALE);
        gc.translate(-Math.floor(camX), -Math.floor(camY));

        // 타일 렌더링 (간단한 가시 범위 계산)
        int startTX = (int)Math.floor(camX / TILE) - 1;
        int startTY = (int)Math.floor(camY / TILE) - 1;
        int endTX   = (int)Math.ceil((camX + VIEW_W) / TILE) + 1;
        int endTY   = (int)Math.ceil((camY + VIEW_H) / TILE) + 1;

        for (int ty = Math.max(0, startTY); ty < Math.min(MAP_H, endTY); ty++) {
            for (int tx = Math.max(0, startTX); tx < Math.min(MAP_W, endTX); tx++) {
                drawTile(tx, ty);
            }
        }

        // 플레이어
        drawPlayer(Math.round((float)px), Math.round((float)py));

        gc.restore();
    }

    private void drawTile(int tx, int ty) {
        double x = tx * TILE;
        double y = ty * TILE;
        if (map[ty][tx] == 0) {
            // 잔디(두 색으로 체크보드 느낌)
            Color g1 = Color.web("#3AA655");
            Color g2 = Color.web("#2E8B57");
            boolean alt = ((tx + ty) & 1) == 0;
            gc.setFill(alt ? g1 : g2);
            gc.fillRect(x, y, TILE, TILE);
            // 잔디 질감 한 줄
            gc.setFill(alt ? g2 : g1);
            gc.fillRect(x, y + TILE - 3, TILE, 1);
        } else {
            // 벽
            gc.setFill(Color.web("#6B6B6B"));
            gc.fillRect(x, y, TILE, TILE);
            gc.setFill(Color.web("#4C4C4C"));
            gc.fillRect(x, y, TILE, 4);
            gc.fillRect(x, y, 4, TILE);
        }
    }

    // 16x16 간단 도트 캐릭터 (두 프레임 걷기)
    private void drawPlayer(double x, double y) {
        // 발걸음 프레임: 0,1 교차 (움직일 때만 빠르게)
        int frame = ((left||right||up||down) ? (animTick / 10) : 0) & 1;

        // 팔/다리 위치 차이만 두는 단순 도트
        // 색
        Color SKIN = Color.web("#F1C27D");
        Color HAIR = Color.web("#4A2E19");
        Color SHIRT = Color.web("#2D7DD2");
        Color PANTS = Color.web("#1B2838");
        Color SHOES = Color.web("#2B2B2B");
        Color OUTLINE = Color.web("#1A1A1A");

        // 한 픽셀 찍기
        java.util.function.BiConsumer<Integer,Integer> dotSkin = (px, py) -> p(x+px, y+py, SKIN);
        java.util.function.BiConsumer<Integer,Integer> dotHair = (px, py) -> p(x+px, y+py, HAIR);
        java.util.function.BiConsumer<Integer,Integer> dotShirt= (px, py) -> p(x+px, y+py, SHIRT);
        java.util.function.BiConsumer<Integer,Integer> dotPants= (px, py) -> p(x+px, y+py, PANTS);
        java.util.function.BiConsumer<Integer,Integer> dotShoes= (px, py) -> p(x+px, y+py, SHOES);
        java.util.function.BiConsumer<Integer,Integer> dotOut  = (px, py) -> p(x+px, y+py, OUTLINE);

        // 16x16 캔버스에 간단히 그리기 (머리 6x5, 몸 8x8, 다리 6x5)
        // 외곽선 & 머리
        for (int i=3;i<=12;i++) dotOut.accept(i, 0);
        dotOut.accept(2,1); dotOut.accept(13,1);
        for (int i=2;i<=13;i++) dotOut.accept(i, 6);

        // 머리카락
        fillRow(dotHair, 4, 1, 10); fillRow(dotHair, 3, 2, 12); fillRow(dotHair, 3, 3, 12);
        // 얼굴(피부)
        fillRow(dotSkin, 4, 4, 10); fillRow(dotSkin, 4, 5, 10);

        // 눈(아주 작은 포인트)
        p(x+5, y+4, OUTLINE); p(x+9, y+4, OUTLINE);

        // 몸통 외곽
        for (int i=4;i<=11;i++) dotOut.accept(i, 7);
        for (int r=8;r<=13;r++){ dotOut.accept(3, r); dotOut.accept(12, r);}
        for (int i=3;i<=12;i++) dotOut.accept(i, 14);

        // 셔츠
        for (int r=8;r<=13;r++) fillRow(dotShirt, 4, r, 8);
        // 팔(피부)
        dotSkin.accept(3,9); dotSkin.accept(3,10); dotSkin.accept(12,9); dotSkin.accept(12,10);

        // 바지 라인
        fillRow(dotPants, 4, 14, 8);

        // 다리/신발 (프레임에 따라 좌우 바뀜)
        int legOffsetL = (frame==0 ? 0 : 1);
        int legOffsetR = (frame==0 ? 1 : 0);
        // 왼쪽 다리
        fillRow(dotPants, 5, 15, 2); p(x+5, y+16+legOffsetL, PANTS); p(x+6, y+16+legOffsetL, PANTS);
        p(x+5, y+17+legOffsetL, SHOES); p(x+6, y+17+legOffsetL, SHOES);
        // 오른쪽 다리
        fillRow(dotPants, 9, 15, 2); p(x+9, y+16+legOffsetR, PANTS); p(x+10, y+16+legOffsetR, PANTS);
        p(x+9, y+17+legOffsetR, SHOES); p(x+10, y+17+legOffsetR, SHOES);

        // 외곽 신발 테두리
        p(x+4, y+17+legOffsetL, OUTLINE); p(x+7, y+17+legOffsetL, OUTLINE);
        p(x+8, y+17+legOffsetR, OUTLINE); p(x+11, y+17+legOffsetR, OUTLINE);
    }

    private void p(double x, double y, Color c) {
        // 1×1 도트
        gc.setFill(c);
        gc.fillRect(x, y, 1, 1);
    }

    private void fillRow(java.util.function.BiConsumer<Integer,Integer> plot, int sx, int y, int len) {
        for (int i=0;i<len;i++) plot.accept(sx + i, y);
    }

    private double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    public static void main(String[] args) {
        launch(args);
    }
}