package algebra

import java.awt.Color
import java.awt.image.BufferedImage.TYPE_INT_RGB
import java.awt.image.{BufferedImage, RenderedImage}
import java.io.File

import algebra.types.Score
import cats.effect.IO
import javax.imageio.ImageIO
import org.scalatest.WordSpec
import org.scalatest.prop.PropertyChecks

class DrawSpec extends WordSpec with Arbitraries with PropertyChecks {

  private val width  = 500
  private val height = 200

  def write(img: RenderedImage): IO[Boolean] = IO(ImageIO.write(img, "png", new File("thing.png")))

  def draw(notes: Score): IO[Boolean] = {
    val img = new BufferedImage(width, height, TYPE_INT_RGB)

    val g = img.getGraphics

    g.setColor(Color.white)
    g.fillRect(0, 0, width, height)
    g.setColor(Color.black)

    val sum = notes.foldLeft(0l) {
      case (a, gote) ⇒
        a + gote.duration.toMillis
    }

    val scale: Float = width.toFloat / sum

    notes.foldLeft(0l) {
      case (offset, Note(n, d)) ⇒
        val x = (offset + d.toMillis) * scale
        val y = n.value * 5
        g.fillRect(x.toInt, y, (d.toMillis * scale).toInt, 5)
        offset + d.toMillis
      case (offset, Rest(d)) ⇒
        offset + d.toMillis
    }

    write(img)
  }

  "Draw" should {
    "draw a score" in {

      forAll { score: Score ⇒
        draw(score).unsafeRunSync()
      }

    }
  }

}
