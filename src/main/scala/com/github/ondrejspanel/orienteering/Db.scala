package com.github.ondrejspanel.orienteering

import java.sql.Timestamp

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.github.ondrejspanel.orienteering.Main.Race
import org.squeryl.{KeyedEntity, PrimitiveTypeMode}

import collection.JavaConverters._

object Db extends PrimitiveTypeMode {
  val jacksonMapperJson = new ObjectMapper() with ScalaObjectMapper
  jacksonMapperJson.registerModule(new DefaultScalaModule)
  jacksonMapperJson.registerModule(new SimpleModule)

  def time0: Long = 0
  type Time = Long


  class Cards(
    val id: Int,
    val runId: Int = 0,
    val runIdAssignts: Timestamp = new Timestamp(0),
    val stageId: Int = 0,
    val stationNumber: Int = 0,
    val siId: Int = 0,
    val checkTime: Time = time0,
    val startTime: Time = time0,
    val finishTime: Time = time0,
    val punches: String = "[]",
    val readerConnectionId: Int = 0,
    val printerConnectionId: Option[Int] = Some(0)
  ) extends KeyedEntity[Int] {
    def this() = this(0)

    lazy val codes: Seq[Int] = {
      val json = jacksonMapperJson.readTree(punches)
      for (p <- json.elements.asScala.toList) yield {
        p.elements.asScala.next.intValue()
      }
    }

  }

  class Runs(
    val id: Int,
    val competitorId: Int = 0,
    val siId: Int = 0,
    val stageId: Int = 0,
    val startTimeMs: Option[Time] = Some(time0),
    val finishTimeMs: Option[Time] = Some(time0),
    val timeMs: Time = time0,
    val offRace: Boolean = false,
    val notCompeting: Boolean = false,
    val disqualified: Boolean = false,
    val mispunch: Boolean = false,
    val badCheck: Boolean = false,
    val cardLent: Boolean = false,
    val cardReturned: Boolean = false
  ) extends KeyedEntity[Int] {
    def this() = this(0)
  }

  class Competitors(
    val id: Int,
    val startNumber: Option[Int] = Some(0),
    val classId: Int = 0,
    val firstName: String = "",
    val lastName: String = "",
    val registration: Option[String] = Some(""),
    val licence: Option[String] = Some(""),
    val club: Option[String] = Some(""),
    val country: Option[String] = Some(""),
    val siId: Int = 0,
    val note: Option[String] = Some(""),
    val ranking: Option[String] = Some(""),
    val importId: Option[String] = Some("")
  ) extends KeyedEntity[Int] {
    def this() = this(0)


    lazy val categories = Race.categoryRelation.right(this)

    def category: String = categories.headOption.map(_.name).getOrElse("")
  }

  class Courses(
    val id: Int,
    val name: String = "",
    val length: Option[Int] = Some(0),
    val climb: Option[Int] = Some(0),
    val note: Option[String] = Some("")
  ) extends KeyedEntity[Int] {
    def this() = this(0)

    lazy val courseCodes = Race.courseRelation.left(this)

    lazy val courseSeq: Seq[Int] = {
      val courseWithPos = for {
        ccc <- courseCodes
        code = ccc.codes.headOption.map(_.code)
        c <- code
      } yield {
        (c, ccc.position)
      }
      courseWithPos.toList.sortBy(_._2).map(_._1)
    }


  }

  class CourseCodes(
    val id: Int,
    val courseId: Int = 0,
    val position: Int = 0,
    val codeId: Int = 0
  ) extends KeyedEntity[Int] {
    def this() = this(0)

    lazy val codes = Race.codeRelation.right(this)
  }

  class Codes(
    val id: Int,
    val code: Int = 0,
    val altCode: Option[Int] = Some(0),
    val outOfOrder: Boolean = false,
    val radio: Boolean = false,
    val note: Option[String] = Some("")
  ) extends KeyedEntity[Int] {
    def this() = this(0)
  }

  class ClassDefs(
    val id: Int,
    val classId: Int = 0,
    val stageId: Int = 0,
    val courseId: Int = 0,
    val startSlotIndex: Int = 0,
    val startTimeMin: Option[Int] = Some(0),
    val startIntervalMin: Option[Int] = Some(0),
    val vacantsBefore: Option[Int] = Some(0),
    val vacantEvery: Option[Int] = Some(0),
    val vacantsAfter: Option[Int] = Some(0),
    val mapCount: Option[Int] = Some(0),
    val resultsCount: Option[Int] = Some(0),
    val lastStartTimeMin: Option[Int] = Some(0),
    val drawLock: Boolean = false
  ) extends KeyedEntity[Int] {
    def this() = this(0)
  }

  class Classes(
    val id: Int,
    val name: String
  ) extends KeyedEntity[Int]


}
