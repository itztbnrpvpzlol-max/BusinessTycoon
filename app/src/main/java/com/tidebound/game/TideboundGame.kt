package com.tidebound.game

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.VertexAttributes
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3

private data class PirateEnemy(
    var x: Float,
    var z: Float,
    var health: Int,
    val maxHealth: Int,
    val name: String,
    val boss: Boolean = false,
    var attackTimer: Float = 0f
)

class TideboundGame : ApplicationAdapter() {
    private lateinit var camera: PerspectiveCamera
    private lateinit var modelBatch: ModelBatch
    private lateinit var spriteBatch: SpriteBatch
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var font: BitmapFont
    private lateinit var environment: Environment

    private val models = mutableListOf<Model>()
    private val world = mutableListOf<ModelInstance>()
    private lateinit var playerBodyModel: Model
    private lateinit var playerHeadModel: Model
    private lateinit var enemyBodyModel: Model
    private lateinit var enemyHeadModel: Model
    private lateinit var playerBody: ModelInstance
    private lateinit var playerHead: ModelInstance
    private lateinit var enemyBody: ModelInstance
    private lateinit var enemyHead: ModelInstance

    private val enemies = mutableListOf<PirateEnemy>()
    private var playerX = 0f
    private var playerZ = 6f
    private var playerHealth = 100
    private var attackCooldown = 0f
    private var joystickActive = false
    private var joystickPointer = -1
    private var joystickStartX = 0f
    private var joystickStartY = 0f
    private var joystickX = 0f
    private var joystickY = 0f
    private var moveX = 0f
    private var moveY = 0f
    private var attackQueued = false
    private var message = "Find Captain Vex and take control of the ship."
    private var messageTimer = 8f
    private var elapsed = 0f
    private var completed = false

    override fun create() {
        camera = PerspectiveCamera(67f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        camera.near = 0.1f
        camera.far = 180f
        modelBatch = ModelBatch()
        spriteBatch = SpriteBatch()
        shapeRenderer = ShapeRenderer()
        font = BitmapFont().apply { data.setScale(1.15f) }

        environment = Environment()
        environment.set(ColorAttribute(ColorAttribute.AmbientLight, Color(0.72f, 0.76f, 0.82f, 1f)))
        environment.add(DirectionalLight().set(Color(0.95f, 0.91f, 0.82f, 1f), Vector3(-0.7f, -1f, -0.4f)))

        buildShip()
        playerBodyModel = makeCylinder(0.8f, 1.7f, 0.8f, Color(0.12f, 0.32f, 0.78f, 1f))
        playerHeadModel = makeSphere(0.82f, 0.82f, 0.82f, Color(0.88f, 0.66f, 0.48f, 1f))
        enemyBodyModel = makeCylinder(0.85f, 1.7f, 0.85f, Color(0.58f, 0.18f, 0.12f, 1f))
        enemyHeadModel = makeSphere(0.82f, 0.82f, 0.82f, Color(0.76f, 0.55f, 0.39f, 1f))
        playerBody = ModelInstance(playerBodyModel)
        playerHead = ModelInstance(playerHeadModel)
        enemyBody = ModelInstance(enemyBodyModel)
        enemyHead = ModelInstance(enemyHeadModel)

        enemies += PirateEnemy(-3.5f, -2f, 35, 35, "Deckhand")
        enemies += PirateEnemy(3.2f, -4.5f, 35, 35, "Raider")
        enemies += PirateEnemy(1.5f, 1f, 45, 45, "First Mate")
        enemies += PirateEnemy(0f, -9f, 160, 160, "Captain Vex", true)

        camera.position.set(0f, 10f, 20f)
        camera.lookAt(0f, 0f, 0f)
        camera.update()

        Gdx.input.inputProcessor = object : InputAdapter() {
            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                val sx = screenX.toFloat()
                val sy = screenY.toFloat()
                if (sx < Gdx.graphics.width * 0.55f && !joystickActive) {
                    joystickActive = true
                    joystickPointer = pointer
                    joystickStartX = sx
                    joystickStartY = sy
                    joystickX = sx
                    joystickY = sy
                    updateJoystick(sx, sy)
                } else if (sx >= Gdx.graphics.width * 0.55f) {
                    attackQueued = true
                }
                return true
            }

            override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
                if (joystickActive && pointer == joystickPointer) {
                    joystickX = screenX.toFloat()
                    joystickY = screenY.toFloat()
                    updateJoystick(joystickX, joystickY)
                }
                return true
            }

            override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                if (pointer == joystickPointer) {
                    joystickActive = false
                    joystickPointer = -1
                    moveX = 0f
                    moveY = 0f
                }
                return true
            }
        }
    }

    private fun updateJoystick(x: Float, y: Float) {
        val dx = x - joystickStartX
        val dy = joystickStartY - y
        val length = kotlin.math.sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
        val scale = minOf(1f, 72f / length)
        moveX = dx / length * scale
        moveY = dy / length * scale
    }

    private fun makeBox(w: Float, h: Float, d: Float, color: Color): Model {
        val material = com.badlogic.gdx.graphics.g3d.Material(ColorAttribute.createDiffuse(color))
        return ModelBuilder().createBox(w, h, d, material, VertexAttributes.Usage.Position.toLong() or VertexAttributes.Usage.Normal.toLong()).also { models += it }
    }

    private fun makeCylinder(w: Float, h: Float, d: Float, color: Color): Model {
        val material = com.badlogic.gdx.graphics.g3d.Material(ColorAttribute.createDiffuse(color))
        return ModelBuilder().createCylinder(w, h, d, 18, material, VertexAttributes.Usage.Position.toLong() or VertexAttributes.Usage.Normal.toLong()).also { models += it }
    }

    private fun makeSphere(w: Float, h: Float, d: Float, color: Color): Model {
        val material = com.badlogic.gdx.graphics.g3d.Material(ColorAttribute.createDiffuse(color))
        return ModelBuilder().createSphere(w, h, d, 16, 12, material, VertexAttributes.Usage.Position.toLong() or VertexAttributes.Usage.Normal.toLong()).also { models += it }
    }

    private fun addWorldBox(w: Float, h: Float, d: Float, x: Float, y: Float, z: Float, color: Color) {
        val instance = ModelInstance(makeBox(w, h, d, color))
        instance.transform.setToTranslation(x, y, z)
        world += instance
    }

    private fun buildShip() {
        addWorldBox(200f, 0.6f, 200f, 0f, -1.25f, 0f, Color(0.035f, 0.22f, 0.34f, 1f))
        addWorldBox(14f, 0.7f, 28f, 0f, -0.38f, 0f, Color(0.34f, 0.19f, 0.095f, 1f))

        for (z in -12..12 step 2) {
            addWorldBox(13.8f, 0.045f, 0.07f, 0f, 0.015f, z.toFloat(), Color(0.55f, 0.34f, 0.17f, 1f))
        }
        for (x in -6..6 step 2) {
            addWorldBox(0.08f, 0.04f, 27f, x.toFloat(), 0.02f, 0f, Color(0.2f, 0.11f, 0.055f, 1f))
        }

        val rail = Color(0.25f, 0.13f, 0.065f, 1f)
        addWorldBox(0.35f, 1.3f, 28f, -6.8f, 0.55f, 0f, rail)
        addWorldBox(0.35f, 1.3f, 28f, 6.8f, 0.55f, 0f, rail)
        addWorldBox(14f, 1.3f, 0.35f, 0f, 0.55f, -13.8f, rail)
        addWorldBox(14f, 1.3f, 0.35f, 0f, 0.55f, 13.8f, rail)

        addWorldBox(0.5f, 12f, 0.5f, 0f, 6f, -4f, Color(0.28f, 0.16f, 0.08f, 1f))
        addWorldBox(0.18f, 7f, 0.18f, 0f, 10f, -4f, Color(0.32f, 0.19f, 0.09f, 1f))
        addWorldBox(7f, 5f, 0.22f, 0f, 7.5f, -4f, Color(0.88f, 0.82f, 0.65f, 1f))
        addWorldBox(3.5f, 2.5f, 0.22f, 0f, 12f, -4f, Color(0.92f, 0.88f, 0.74f, 1f))

        val crate = Color(0.48f, 0.28f, 0.12f, 1f)
        addWorldBox(1.5f, 1.5f, 1.5f, -4.8f, 0.85f, 7f, crate)
        addWorldBox(1.5f, 1.5f, 1.5f, -3.5f, 0.85f, 7f, crate)
        addWorldBox(1.8f, 1.8f, 1.8f, 4.7f, 0.95f, -7f, crate)
        addWorldBox(2.5f, 0.4f, 1.8f, 0f, 0.25f, 11.8f, Color(0.22f, 0.12f, 0.06f, 1f))
    }

    override fun render() {
        val delta = Gdx.graphics.deltaTime.coerceAtMost(0.05f)
        elapsed += delta
        attackCooldown = (attackCooldown - delta).coerceAtLeast(0f)
        messageTimer = (messageTimer - delta).coerceAtLeast(0f)

        playerX = (playerX + moveX * 5.2f * delta).coerceIn(-5.7f, 5.7f)
        playerZ = (playerZ - moveY * 5.2f * delta).coerceIn(-12.3f, 12.3f)

        updateEnemies(delta)
        if (attackQueued) {
            attackQueued = false
            attack()
        }

        playerBody.transform.setToTranslation(playerX, 0.95f, playerZ)
        playerHead.transform.setToTranslation(playerX, 2.12f, playerZ)
        camera.position.set(playerX + 0.4f, 9.2f, playerZ + 13.5f)
        camera.lookAt(playerX, 0.5f, playerZ - 2f)
        camera.up.set(Vector3.Y)
        camera.update()

        Gdx.gl.glViewport(0, 0, Gdx.graphics.width, Gdx.graphics.height)
        Gdx.gl.glClearColor(0.025f, 0.16f, 0.23f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST)

        modelBatch.begin(camera)
        for (instance in world) modelBatch.render(instance, environment)
        modelBatch.render(playerBody, environment)
        modelBatch.render(playerHead, environment)
        for (enemy in enemies) {
            if (enemy.health <= 0) continue
            enemyBody.transform.setToTranslation(enemy.x, 0.95f, enemy.z)
            enemyHead.transform.setToTranslation(enemy.x, 2.12f, enemy.z)
            modelBatch.render(enemyBody, environment)
            modelBatch.render(enemyHead, environment)
        }
        modelBatch.end()

        drawHud()
    }

    private fun updateEnemies(delta: Float) {
        for (enemy in enemies) {
            if (enemy.health <= 0) continue
            val dx = playerX - enemy.x
            val dz = playerZ - enemy.z
            val distance = kotlin.math.sqrt(dx * dx + dz * dz).coerceAtLeast(0.001f)
            if (distance > 1.65f && distance < 14f) {
                val speed = if (enemy.boss) 1.35f else 1.7f
                enemy.x += dx / distance * speed * delta
                enemy.z += dz / distance * speed * delta
                enemy.x = enemy.x.coerceIn(-5.8f, 5.8f)
                enemy.z = enemy.z.coerceIn(-12.4f, 12.4f)
            } else if (distance <= 1.8f) {
                enemy.attackTimer -= delta
                if (enemy.attackTimer <= 0f) {
                    playerHealth = (playerHealth - if (enemy.boss) 12 else 5).coerceAtLeast(0)
                    enemy.attackTimer = if (enemy.boss) 1.1f else 1.5f
                    showMessage("${enemy.name} hit you!")
                    if (playerHealth == 0) {
                        playerHealth = 100
                        playerX = 0f
                        playerZ = 6f
                        showMessage("You were knocked back. Catch your breath!")
                    }
                }
            }
        }
    }

    private fun attack() {
        if (attackCooldown > 0f) return
        attackCooldown = 0.48f
        val target = enemies.filter { it.health > 0 }
            .minByOrNull { enemy ->
                val dx = enemy.x - playerX
                val dz = enemy.z - playerZ
                dx * dx + dz * dz
            }
        if (target == null) {
            showMessage("No enemies remain. The deck is yours!")
            completed = true
            return
        }
        val dx = target.x - playerX
        val dz = target.z - playerZ
        val distance = kotlin.math.sqrt(dx * dx + dz * dz)
        if (distance <= 3.8f) {
            target.health = (target.health - 25).coerceAtLeast(0)
            showMessage(if (target.health == 0) "${target.name} defeated!" else "You struck ${target.name}!")
            if (enemies.all { it.health <= 0 }) {
                completed = true
                showMessage("CHAPTER COMPLETE — The ship is yours!")
            }
        } else {
            showMessage("Move closer to attack!")
        }
    }

    private fun showMessage(text: String) {
        message = text
        messageTimer = 3.5f
    }

    private fun drawHud() {
        val width = Gdx.graphics.width.toFloat()
        val height = Gdx.graphics.height.toFloat()
        spriteBatch.begin()
        font.color = Color.WHITE
        font.draw(spriteBatch, "TIDEBOUND  /  CHAPTER 01: THE IRON GULL", 26f, height - 24f)
        font.draw(spriteBatch, "HP  $playerHealth / 100", 26f, height - 54f)
        val boss = enemies.firstOrNull { it.boss }
        if (boss != null && boss.health > 0) {
            font.draw(spriteBatch, "CAPTAIN VEX  ${boss.health} / ${boss.maxHealth}", width - 290f, height - 26f)
        }
        if (messageTimer > 0f || completed) {
            font.draw(spriteBatch, message, width * 0.31f, height - 72f)
        }
        font.draw(spriteBatch, "MOVE", 45f, 45f)
        font.draw(spriteBatch, "ATTACK", width - 116f, 45f)
        spriteBatch.end()

        shapeRenderer.projectionMatrix.setToOrtho2D(0f, 0f, width, height)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color(1f, 1f, 1f, 0.72f)
        if (joystickActive) {
            shapeRenderer.circle(joystickStartX, height - joystickStartY, 66f, 40)
            shapeRenderer.circle(joystickX, height - joystickY, 25f, 32)
        } else {
            shapeRenderer.circle(95f, 95f, 62f, 40)
            shapeRenderer.circle(width - 92f, 95f, 55f, 40)
        }
        shapeRenderer.end()
    }

    override fun resize(width: Int, height: Int) {
        if (::camera.isInitialized && height > 0) {
            camera.viewportWidth = width.toFloat()
            camera.viewportHeight = height.toFloat()
            camera.update()
        }
    }

    override fun dispose() {
        if (::modelBatch.isInitialized) modelBatch.dispose()
        if (::spriteBatch.isInitialized) spriteBatch.dispose()
        if (::shapeRenderer.isInitialized) shapeRenderer.dispose()
        if (::font.isInitialized) font.dispose()
        models.forEach { it.dispose() }
    }
}
