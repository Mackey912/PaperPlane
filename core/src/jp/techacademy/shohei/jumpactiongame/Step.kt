package jp.techacademy.shohei.jumpactiongame

import com.badlogic.gdx.graphics.Texture

class Step(type: Int, texture: Texture, srcX: Int, srcY: Int, srcWidth: Int, srcHeight: Int)
    : GameObject(texture, srcX, srcY, srcWidth, srcHeight) {

    companion object {
        // 横幅、高さ
        val STEP_WIDTH = 2.0f
        val STEP_HEIGHT = 0.5f

        // タイプ（通常と動くタイプ）
        val STEP_TYPE_STATIC = 0
        val STEP_TYPE_MOVING = 1

        // 状態（通常と消えた状態）
        val STEP_STATE_NORMAL = 0
        val STEP_STATE_VANISH = 1

        // 速度
        val STEP_VELOCITY = 2.0f
    }

    var mState: Int = 0
    var mType: Int

    init {
        setSize(STEP_WIDTH, STEP_HEIGHT)
        mType = type
        if (mType == STEP_TYPE_MOVING) {
            velocity.x = STEP_VELOCITY
        }
    }

    // 座標を更新する
    fun update(deltaTime: Float) {
        if (mType == STEP_TYPE_MOVING) {
            x += velocity.x * deltaTime

            if (x < STEP_WIDTH / 2) {
                velocity.x = -velocity.x
                x = STEP_WIDTH / 2
            }
            if (x > GameScreen.WORLD_WIDTH - STEP_WIDTH / 2) {
                velocity.x = -velocity.x
                x = GameScreen.WORLD_WIDTH - STEP_WIDTH / 2
            }
        }
    }

    // 消える
    fun vanish() {
        mState = STEP_STATE_VANISH
        setAlpha(0f)
        velocity.x = 0f
    }
}