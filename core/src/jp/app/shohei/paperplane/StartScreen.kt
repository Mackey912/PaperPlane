package jp.app.shohei.paperplane

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.FitViewport

class StartScreen(private val mGame: PaperPlane) : ScreenAdapter() {
    companion object {
        internal val GUI_WIDTH = 320f
        internal val GUI_HEIGHT = 480f
    }

    private var mBg: Sprite
    private var mGuiCamera: OrthographicCamera
    private var mGuiViewPort: FitViewport
    private var mFont: BitmapFont
    private var mFont_title:BitmapFont
    private var mHighScore:Int
    private var mPrefs: Preferences
    private var mIcon:icon
    private var time:Float=0f
    private val sound_startmenu=Gdx.audio.newMusic(Gdx.files.internal("startmenu.mp3"))
    private val sound_startgame=Gdx.audio.newSound(Gdx.files.internal("startgame2.mp3"))


    init {
        if (mGame.mRequestHandler != null) { // ←追加する
            mGame.mRequestHandler.showAds(false) // ←追加する
        } // ←追加する

        // 背景の準備
        val bgTexture = Texture("blue.png")
        val iconTexture = Texture("kamihikouki.png")
        var icon = icon(iconTexture, 0, 0, 350, 258)
        icon.setPosition(80f, 180f)
        mIcon=icon


        mBg = Sprite(TextureRegion(bgTexture, 300, 0, 540, 810))
        mBg.setSize(GUI_WIDTH, GUI_HEIGHT)
        mBg.setPosition(0f, 0f)

        // GUI用のカメラを設定する
        mGuiCamera = OrthographicCamera()
        mGuiCamera.setToOrtho(false, GUI_WIDTH, GUI_HEIGHT)
        mGuiViewPort = FitViewport(GUI_WIDTH, GUI_HEIGHT, mGuiCamera)

        // フォント
        mFont = BitmapFont(Gdx.files.internal("font.fnt"), Gdx.files.internal("font.png"), false)
        mFont.setColor(Color.GRAY)
        mFont_title = BitmapFont(Gdx.files.internal("font.fnt"), Gdx.files.internal("font.png"), false)
        mFont_title.setColor(Color.GRAY)
        mHighScore=0
        mPrefs=Gdx.app.getPreferences("jp.techacademy.shohei.paperplane")

        sound_startmenu.setVolume(0.2f)
        sound_startmenu.setLooping(true)
        sound_startmenu.play()
    }

    override fun render(delta: Float) {
        // 描画する

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // カメラの座標をアップデート（計算）し、スプライトの表示に反映させる
        mGuiCamera.update()
        mGame.batch.projectionMatrix = mGuiCamera.combined

        mGame.batch.begin()
        mBg.draw(mGame.batch)
        //ハイスコアをPreferencesから取得する

        mHighScore=mPrefs.getInteger("HIGHSCORE",0)
        mFont_title.data.setScale(1.5f)
        mFont_title.draw(mGame.batch, "Paper Plane", 0f, GUI_HEIGHT / 2 + 140, GUI_WIDTH, Align.center, false)
        mFont.draw(mGame.batch, "Touch To Start!", 0f, GUI_HEIGHT / 2 - 80, GUI_WIDTH, Align.center, false)
        mFont.draw(mGame.batch, "High Score: $mHighScore", 0f, GUI_HEIGHT / 2 - 140, GUI_WIDTH, Align.center, false)
        mIcon.draw(mGame.batch)

        mGame.batch.end()

        time += Gdx.graphics.getDeltaTime()
        if(0.4f>time&&time>=0f){
            mIcon.setPosition(80f, 180f)
        }else if(0.8f>time&&time>=0.4f){
            mIcon.setPosition(80f, 185f)
        }else if(1f>time&&time>=0.8f){
            mIcon.setPosition(80f, 190f)
        }else{
            time=0f
        }

        if (Gdx.input.justTouched()) {
            if (mGame.mRequestHandler != null) { // ←追加する
                mGame.mRequestHandler.showAds(false) // ←追加する
            } // ←追加する
            mGame.screen = GameScreen(mGame)
            sound_startmenu.stop()
            sound_startgame.play(0.4f)

        }
    }
}