package jp.techacademy.shohei.paperplane

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import java.lang.Math.abs
import java.util.*
import kotlin.collections.ArrayList

class GameScreen(private val mGame: PaperPlane) : ScreenAdapter() {
    companion object {
        val CAMERA_WIDTH = 10f
        val CAMERA_HEIGHT = 15f
        val WORLD_WIDTH = 10f
        val WORLD_HEIGHT = 15 * 3
        val GUI_WIDTH = 320f
        val GUI_HEIGHT = 480f

        val GAME_STATE_READY = 0
        val GAME_STATE_PLAYING = 1
        val GAME_STATE_GAMEOVER = 2

        // 横幅、高さ
        val PLAYER_WIDTH = 1.0f
        val PLAYER_HEIGHT = 1.0f
        var fcount:Int=0

    }

    private val mBg: Sprite
    private val mCamera: OrthographicCamera
    private val mGuiCamera: OrthographicCamera
    private val mViewPort: FitViewport
    private val mGuiViewPort: FitViewport

    private var mRandom: Random
    private var mBlocks:ArrayList<Block>
    private var mFragments:ArrayList<Fragment>
    private lateinit var mPlayer: Player

    private var mGameState: Int
    private var mHeightSoFar: Float = 0f
    private var mTouchPoint: Vector3
    private var mFont:BitmapFont
    private var mScore:Int
    private var mHighScore:Int
    private var mPrefs:Preferences
    //どの角度を保持しているか-2～+2
    private var angleKey:Int=0
    private var pangleKey:Int=0
    private var time:Float=0f
    private var touchInterval:Float=0f
    private var prevAngleState:Boolean=false
    private var stageFlag:Boolean=false
    private var stageCount:Int=0
    private var y:Float=0f


    //音の設定
    val sound_hitenemy=Gdx.audio.newSound(Gdx.files.internal("sound_hitenemy.mp3"))
    val sound_pera=Gdx.audio.newSound(Gdx.files.internal("pera.mp3"))
    val sound_fall=Gdx.audio.newSound(Gdx.files.internal("sound_fall.mp3"))
    val sound_hitstar=Gdx.audio.newSound(Gdx.files.internal("sound_hitstar.mp3"))
    val sound_gameclear=Gdx.audio.newSound(Gdx.files.internal("sound_gameclear.mp3"))


    init {
        // 背景の準備
        val bgTexture = Texture("background_sky.png")
        // TextureRegionで切り出す時の原点は左上
        mBg = Sprite(TextureRegion(bgTexture, 0, 0, 540, 810))
        mBg.setSize(CAMERA_WIDTH, CAMERA_HEIGHT)
        mBg.setPosition(0f, 0f)

        // カメラ、ViewPortを生成、設定する
        mCamera = OrthographicCamera()
        mCamera.setToOrtho(false, CAMERA_WIDTH, CAMERA_HEIGHT)
        mViewPort = FitViewport(CAMERA_WIDTH, CAMERA_HEIGHT, mCamera)

        // GUI用のカメラを設定する
        mGuiCamera = OrthographicCamera()
        mGuiCamera.setToOrtho(false, GUI_WIDTH, GUI_HEIGHT)
        mGuiViewPort = FitViewport(GUI_WIDTH, GUI_HEIGHT, mGuiCamera)

        // プロパティの初期化
        mRandom = Random()
        mBlocks=ArrayList<Block>()
        mGameState = GAME_STATE_READY
        mTouchPoint = Vector3()
        mFragments=ArrayList<Fragment>()
        mFont= BitmapFont(Gdx.files.internal("font.fnt"),Gdx.files.internal("font.png"),false)
        mFont.data.setScale(0.8f)
        mScore=3
        mHighScore=0

        //ハイスコアをPreferencesから取得する
        mPrefs=Gdx.app.getPreferences("jp.techacademy.shohei.paperplane")
        mHighScore=mPrefs.getInteger("HIGHSCORE",0)

        createStage()
    }

    override fun render(delta: Float) {
        // それぞれの状態をアップデートする
        update(delta)

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // カメラの中心を超えたらカメラを上に移動させる つまりキャラが画面の上半分には絶対に行かない
        if (mPlayer.y < mCamera.position.y) {
            mCamera.position.y = mPlayer.y
        }


        Gdx.app.log("mPlayer.y",mPlayer.y.toString())
        // カメラの座標をアップデート（計算）し、スプライトの表示に反映させる
        mCamera.update()
        mGame.batch.projectionMatrix = mCamera.combined

        mGame.batch.begin()

        // 原点は左下
        mBg.setPosition(mCamera.position.x - CAMERA_WIDTH / 2, mCamera.position.y - CAMERA_HEIGHT / 2)
        mBg.draw(mGame.batch)

        // Step
        for (i in 0 until mBlocks.size) {
            mBlocks[i].draw(mGame.batch)
        }

        //Player
        if(mGameState== GAME_STATE_PLAYING||mGameState== GAME_STATE_READY) {
            mPlayer.draw(mGame.batch)
        }else if(mGameState== GAME_STATE_GAMEOVER){
            for(i in 0 until mFragments.size)
                mFragments[i].draw(mGame.batch)
        }

        mGame.batch.end()

        //スコア表示

        mScore=-(mPlayer.y*0.2f).toInt()
        mScore+=1
        if (mScore > mHighScore) {
            mHighScore = mScore
            mPrefs.putInteger("HIGHSCORE",mHighScore)
            mPrefs.flush()
        }
        mGuiCamera.update()
        mGame.batch.projectionMatrix=mGuiCamera.combined
        mGame.batch.begin()
        mFont.draw(mGame.batch,"HighScore:$mHighScore",16f, GUI_HEIGHT-15)
        mFont.draw(mGame.batch,"Score:$mScore",16f, GUI_HEIGHT-35)
        mGame.batch.end()

        //次ステージ作成

        if(abs(mPlayer.y)% WORLD_HEIGHT/ WORLD_HEIGHT>0.6f&&stageFlag==true){
            stageCount++
            createStage()
            stageFlag=false
        }
        if(abs(mPlayer.y)% WORLD_HEIGHT/ WORLD_HEIGHT<0.2f&&stageFlag==false){
            stageFlag=true
        }

    }

    override fun resize(width: Int, height: Int) {
        mViewPort.update(width, height)
        mGuiViewPort.update(width, height)
    }

    // ステージを作成する
    private fun createStage() {

        // テクスチャの準備
        val blockTexture = Texture("block.png")
        val playerTexture = Texture("paperplane.png")

        // Stepをゴールの高さまで配置していく
        val stepInterval= WORLD_HEIGHT/8f


        if(stageCount==0) {
            // Playerを配置
            var y = 0f
            mPlayer = Player(playerTexture, 0, 0, 63, 79)
            mPlayer.setPosition(WORLD_WIDTH / 2 - PLAYER_WIDTH / 2, 5f)
            mCamera.position.y = mPlayer.y
        }else if(stageCount%2==1){
            //縦の拡大が大きいので，開始位置を少し下げる
            y-=4f
        }

        while (-(stageCount+1).toFloat()*WORLD_HEIGHT<=y&&y <= -stageCount.toFloat()*WORLD_HEIGHT) {

            if (stageCount % 2 == 0) {
                //一つ目のブロック0~x1,二つ目のブロックx2~WIDTHに分割
                val x1 = mRandom.nextFloat() * WORLD_WIDTH / 3 + WORLD_WIDTH / 6
                val blockInterval = mRandom.nextFloat() * 3 / 5 + WORLD_WIDTH / 4
                val x2 = x1 + blockInterval

                val blockR = Block(blockTexture, 0, 0, 256, 52)
                blockR.setPosition(0f, y)
                blockR.setOrigin(0f, 0f)
                blockR.setScale(x1, 1f)
                mBlocks.add(blockR)

                val blockL = Block(blockTexture, 0, 0, 256, 52)
                blockL.setPosition(x2, y)
                blockL.setOrigin(0f, 0f)
                blockL.setScale(WORLD_WIDTH - x2, 1f)
                mBlocks.add(blockL)

                val wallR = Block(blockTexture, 0, 0, 256, 52)
                wallR.setPosition(0f, y)
                wallR.setOrigin(0f, 0f)
                wallR.setScale(0.5f, stepInterval)
                mBlocks.add(wallR)

                val wallL = Block(blockTexture, 0, 0, 256, 52)
                wallL.setPosition(WORLD_WIDTH-0.5f, y)
                wallL.setOrigin(0f, 0f)
                wallL.setScale(0.5f, stepInterval)
                mBlocks.add(wallL)

                y -= stepInterval

            } else if(stageCount % 2 == 1){
                //一つ目のブロック0~x1,二つ目のブロックx2~WIDTHに分割
                val x1 = WORLD_WIDTH / 3
                val blockInterval = WORLD_WIDTH / 3
                val x2 = x1 + blockInterval

                val blockR = Block(blockTexture, 0, 0, 256, 52)
                blockR.setPosition(0f, y)
                blockR.setOrigin(0f, 0f)
                blockR.setScale(x1, stepInterval - 0.5f)
                mBlocks.add(blockR)

                val blockL = Block(blockTexture, 0, 0, 256, 52)
                blockL.setPosition(x2, y)
                blockL.setOrigin(0f, 0f)
                blockL.setScale(WORLD_WIDTH / 3, stepInterval - 0.5f)
                mBlocks.add(blockL)

                y -= stepInterval
            }
        }
        if(stageCount%2==0){
            //ステージ間で隙間が空かないようにする
            val wallR = Block(blockTexture, 0, 0, 256, 52)
            wallR.setPosition(0f, y)
            wallR.setOrigin(0f, 0f)
            wallR.setScale(0.5f, stepInterval)
            mBlocks.add(wallR)

            val wallL = Block(blockTexture, 0, 0, 256, 52)
            wallL.setPosition(WORLD_WIDTH-0.5f, y)
            wallL.setOrigin(0f, 0f)
            wallL.setScale(0.5f, stepInterval)
            mBlocks.add(wallL)
        }
    }



    // それぞれのオブジェクトの状態をアップデートする
    private fun update(delta: Float) {
        when (mGameState) {
            GAME_STATE_READY ->
                updateReady()
            GAME_STATE_PLAYING ->
                updatePlaying(delta)
            GAME_STATE_GAMEOVER ->
                updateGameOver(delta)
        }
    }

    private fun updateReady() {
        if (Gdx.input.justTouched()) {
            mGameState = GAME_STATE_PLAYING
            sound_pera.play(0.2f)
        }
    }

    private fun updatePlaying(delta: Float) {

        time += Gdx.graphics.getDeltaTime()
        touchInterval += Gdx.graphics.getDeltaTime()
        Gdx.app.log("time", time.toString())

        //タッチ感覚の調整
        if (touchInterval >= 3f * Gdx.graphics.getDeltaTime()) {
            if (Gdx.input.isTouched) {
                mGuiViewPort.unproject(mTouchPoint.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))
                val left = Rectangle(0f, 0f, GUI_WIDTH / 2, GUI_HEIGHT)
                val right = Rectangle(GUI_WIDTH / 2, 0f, GUI_WIDTH / 2, GUI_HEIGHT)
                if (left.contains(mTouchPoint.x, mTouchPoint.y)) {
                    if (angleKey != 4) angleKey++
                }
                if (right.contains(mTouchPoint.x, mTouchPoint.y)) {
                    if (angleKey != -4) angleKey--
                }
                if (pangleKey == angleKey) {
                    //前と同じ角度なら音を鳴らさない
                } else if (prevAngleState==true&&angleKey>pangleKey==false){
                    //右タップの後に左タップした場合音を鳴らす
                    sound_pera.play(0.2f)
                } else if (prevAngleState==false&&angleKey>pangleKey==true){
                    //タップの後に左右タップした場合音を鳴らす
                    sound_pera.play(0.2f)
                } else if (touchInterval <= 6f * Gdx.graphics.getDeltaTime()) {
                    //連続タップ時音を鳴らさない
                } else {
                    sound_pera.play(0.2f)
                }
                prevAngleState=angleKey>pangleKey
                pangleKey = angleKey
                touchInterval = 0f
            }
        }

        mPlayer.update(delta, angleKey)
        mHeightSoFar = Math.max(mPlayer.y, mHeightSoFar)

        // 当たり判定を行う
        checkCollision()

    }



    private fun updateGameOver(delta:Float) {
        for(fragment in mFragments) {
            fragment.update(delta)
        }
        if(Gdx.input.justTouched()){
            mGame.screen=ResultScreen(mGame,mScore)
        }
    }

    private fun checkCollision() {

        // Stepとの当たり判定
        for (i in 0 until mBlocks.size) {
            val block = mBlocks[i]
            val mPlayer_small=Rectangle()
            //当たり判定を修正する
            mPlayer_small.x=mPlayer.x+0.4f
            mPlayer_small.y=mPlayer.y+0.1f
            mPlayer_small.width=0.2f
            mPlayer_small.height=mPlayer.height-0.1f

            if (block.boundingRectangle.overlaps(mPlayer_small)) {
                sound_hitstar.play(0.2f)

                //死亡時，破片をまき散らす
                val fragmentTexture = Texture("fragment.png")
                for(i in 0 until 30){
                    var fragment=Fragment(fragmentTexture, 0, 0, 16, 16)
                    fragment.setPosition(mPlayer.x,mPlayer.y)
                    fragment.setup()
                    mFragments.add(fragment)
                    fcount++
                }
                mGameState= GAME_STATE_GAMEOVER
            }
        }
    }
}