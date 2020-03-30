package jp.techacademy.shohei.paperplane

import com.badlogic.gdx.graphics.Texture

class Step(texture: Texture, srcX: Int, srcY: Int, srcWidth: Int, srcHeight: Int)
    : GameObject(texture, srcX, srcY, srcWidth, srcHeight) {

    companion object {
        // 横幅、高さ
        val STEP_WIDTH = 2.0f
        val STEP_HEIGHT = 0.5f
    }

    init {
        setSize(STEP_WIDTH, STEP_HEIGHT)
    }

}