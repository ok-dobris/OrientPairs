package com.github.ondrejspanel.orienteering

import org.squeryl.adapters.PostgreSqlAdapter
import org.squeryl._

object Main extends App with PrimitiveTypeMode {

  val secret = getClass.getResourceAsStream("/secret.txt")

  val in = io.Source.fromInputStream(secret).getLines

  val dbName = in.next()
  val user = in.next()
  val password = in.next()
  val missingPenalty = in.next().toInt

  import Db._

  object Race extends Schema {
    override def name = Some(dbName)

    override def tableNameFromClassName(tableName: String) = tableName.toLowerCase
    override def columnNameFromPropertyName(propertyName: String) = propertyName.toLowerCase

    val cards = table[Cards]
    val courses = table[Courses]
    val courseCodes = table[CourseCodes]
    val competitors = table[Competitors]
    val runs = table[Runs]
    val classDefs = table[ClassDefs]
    val codes = table[Codes]

    val courseRelation = oneToManyRelation(courses, courseCodes).via((c,cc) => c.id === cc.courseId)
    val codeRelation = oneToManyRelation(codes, courseCodes).via((c, cc) => cc.codeId === c.id)
  }

  def connectToDb(): Unit = {
    Class.forName("org.postgresql.Driver")

    SessionFactory.concreteFactory = Some(() =>
      Session.create(
        java.sql.DriverManager.getConnection("jdbc:postgresql://localhost/quickevent", user, password),
        new PostgreSqlAdapter))


    import Race._

    val results = inTransaction {
      val cs = join(cards, runs, competitors, classDefs, courses)((c, r, p, d, course) =>
        select(c, r, p, d, course)
        on(c.runId === r.id, r.competitorId === p.id, p.classId === d.classId, d.courseId === course.id)
      )

      cs.map { case (card, run, person, classDef, course) =>

        val missingCodes = {
          val correct = Util.lcs(card.codes, course.courseSeq)
          val expected = card.codes.length min course.courseSeq.length
          expected - correct.length
        }

        val note = s"missing $missingCodes"

        update(competitors) ( p=>
          where(p.id ===  person.id)
          set(p.note := Some(note))
        )

        (person.id, person.firstName, person.lastName, run.timeMs / 1000 + missingCodes * missingPenalty, missingCodes)
      }
    }

    for (r <- results) {
      println(r.productIterator.mkString(" "))
    }
  }

  connectToDb()

}
