package io.accur8.neodeploy

/**

  WARNING THIS IS GENERATED CODE.  DO NOT EDIT.

  The only manually maintained code is the code between the //==== (normally where you add your imports)

*/

//====
import io.accur8.neodeploy.HealthchecksDotIo.CheckUpsertRequest
import io.accur8.neodeploy.HealthchecksDotIo.ApiAuthToken
import io.accur8.neodeploy.HealthchecksDotIo.CheckReadOnly
//====

import a8.shared.Meta.{CaseClassParm, Generator, Constructors}



object MxHealthchecksDotIo {
  
  trait MxCheckReadOnly {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[CheckReadOnly,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[CheckReadOnly,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[CheckReadOnly,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.name)
          .addField(_.slug)
          .addField(_.tags)
          .addField(_.desc)
          .addField(_.grace)
          .addField(_.n_pings)
          .addField(_.status)
          .addField(_.last_ping)
          .addField(_.next_ping)
          .addField(_.manual_resume)
          .addField(_.methods)
          .addField(_.success_kw)
          .addField(_.failure_kw)
          .addField(_.filter_subject)
          .addField(_.filter_body)
          .addField(_.ping_url)
          .addField(_.update_url)
          .addField(_.pause_url)
          .addField(_.resume_url)
          .addField(_.channels)
          .addField(_.schedule)
          .addField(_.tz)
          .addField(_.subject)
          .addField(_.subject_fail)
          .addField(_.timeout)
          .addField(_.last_duration)
      )
      .build
    
    implicit val zioEq: zio.prelude.Equal[CheckReadOnly] = zio.prelude.Equal.default
    
    implicit val catsEq: cats.Eq[CheckReadOnly] = cats.Eq.fromUniversalEquals
    
    lazy val generator: Generator[CheckReadOnly,parameters.type] =  {
      val constructors = Constructors[CheckReadOnly](26, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val name: CaseClassParm[CheckReadOnly,String] = CaseClassParm[CheckReadOnly,String]("name", _.name, (d,v) => d.copy(name = v), None, 0)
      lazy val slug: CaseClassParm[CheckReadOnly,String] = CaseClassParm[CheckReadOnly,String]("slug", _.slug, (d,v) => d.copy(slug = v), None, 1)
      lazy val tags: CaseClassParm[CheckReadOnly,String] = CaseClassParm[CheckReadOnly,String]("tags", _.tags, (d,v) => d.copy(tags = v), None, 2)
      lazy val desc: CaseClassParm[CheckReadOnly,String] = CaseClassParm[CheckReadOnly,String]("desc", _.desc, (d,v) => d.copy(desc = v), None, 3)
      lazy val grace: CaseClassParm[CheckReadOnly,Long] = CaseClassParm[CheckReadOnly,Long]("grace", _.grace, (d,v) => d.copy(grace = v), None, 4)
      lazy val n_pings: CaseClassParm[CheckReadOnly,Long] = CaseClassParm[CheckReadOnly,Long]("n_pings", _.n_pings, (d,v) => d.copy(n_pings = v), None, 5)
      lazy val status: CaseClassParm[CheckReadOnly,String] = CaseClassParm[CheckReadOnly,String]("status", _.status, (d,v) => d.copy(status = v), None, 6)
      lazy val last_ping: CaseClassParm[CheckReadOnly,Option[String]] = CaseClassParm[CheckReadOnly,Option[String]]("last_ping", _.last_ping, (d,v) => d.copy(last_ping = v), Some(()=> None), 7)
      lazy val next_ping: CaseClassParm[CheckReadOnly,Option[String]] = CaseClassParm[CheckReadOnly,Option[String]]("next_ping", _.next_ping, (d,v) => d.copy(next_ping = v), Some(()=> None), 8)
      lazy val manual_resume: CaseClassParm[CheckReadOnly,Boolean] = CaseClassParm[CheckReadOnly,Boolean]("manual_resume", _.manual_resume, (d,v) => d.copy(manual_resume = v), None, 9)
      lazy val methods: CaseClassParm[CheckReadOnly,String] = CaseClassParm[CheckReadOnly,String]("methods", _.methods, (d,v) => d.copy(methods = v), None, 10)
      lazy val success_kw: CaseClassParm[CheckReadOnly,String] = CaseClassParm[CheckReadOnly,String]("success_kw", _.success_kw, (d,v) => d.copy(success_kw = v), None, 11)
      lazy val failure_kw: CaseClassParm[CheckReadOnly,String] = CaseClassParm[CheckReadOnly,String]("failure_kw", _.failure_kw, (d,v) => d.copy(failure_kw = v), None, 12)
      lazy val filter_subject: CaseClassParm[CheckReadOnly,Boolean] = CaseClassParm[CheckReadOnly,Boolean]("filter_subject", _.filter_subject, (d,v) => d.copy(filter_subject = v), None, 13)
      lazy val filter_body: CaseClassParm[CheckReadOnly,Boolean] = CaseClassParm[CheckReadOnly,Boolean]("filter_body", _.filter_body, (d,v) => d.copy(filter_body = v), None, 14)
      lazy val ping_url: CaseClassParm[CheckReadOnly,String] = CaseClassParm[CheckReadOnly,String]("ping_url", _.ping_url, (d,v) => d.copy(ping_url = v), None, 15)
      lazy val update_url: CaseClassParm[CheckReadOnly,String] = CaseClassParm[CheckReadOnly,String]("update_url", _.update_url, (d,v) => d.copy(update_url = v), None, 16)
      lazy val pause_url: CaseClassParm[CheckReadOnly,String] = CaseClassParm[CheckReadOnly,String]("pause_url", _.pause_url, (d,v) => d.copy(pause_url = v), None, 17)
      lazy val resume_url: CaseClassParm[CheckReadOnly,String] = CaseClassParm[CheckReadOnly,String]("resume_url", _.resume_url, (d,v) => d.copy(resume_url = v), None, 18)
      lazy val channels: CaseClassParm[CheckReadOnly,String] = CaseClassParm[CheckReadOnly,String]("channels", _.channels, (d,v) => d.copy(channels = v), None, 19)
      lazy val schedule: CaseClassParm[CheckReadOnly,Option[String]] = CaseClassParm[CheckReadOnly,Option[String]]("schedule", _.schedule, (d,v) => d.copy(schedule = v), Some(()=> None), 20)
      lazy val tz: CaseClassParm[CheckReadOnly,Option[String]] = CaseClassParm[CheckReadOnly,Option[String]]("tz", _.tz, (d,v) => d.copy(tz = v), Some(()=> None), 21)
      lazy val subject: CaseClassParm[CheckReadOnly,Option[String]] = CaseClassParm[CheckReadOnly,Option[String]]("subject", _.subject, (d,v) => d.copy(subject = v), Some(()=> None), 22)
      lazy val subject_fail: CaseClassParm[CheckReadOnly,Option[String]] = CaseClassParm[CheckReadOnly,Option[String]]("subject_fail", _.subject_fail, (d,v) => d.copy(subject_fail = v), Some(()=> None), 23)
      lazy val timeout: CaseClassParm[CheckReadOnly,Option[Long]] = CaseClassParm[CheckReadOnly,Option[Long]]("timeout", _.timeout, (d,v) => d.copy(timeout = v), Some(()=> None), 24)
      lazy val last_duration: CaseClassParm[CheckReadOnly,Option[Long]] = CaseClassParm[CheckReadOnly,Option[Long]]("last_duration", _.last_duration, (d,v) => d.copy(last_duration = v), Some(()=> None), 25)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): CheckReadOnly = {
        CheckReadOnly(
          name = values(0).asInstanceOf[String],
          slug = values(1).asInstanceOf[String],
          tags = values(2).asInstanceOf[String],
          desc = values(3).asInstanceOf[String],
          grace = values(4).asInstanceOf[Long],
          n_pings = values(5).asInstanceOf[Long],
          status = values(6).asInstanceOf[String],
          last_ping = values(7).asInstanceOf[Option[String]],
          next_ping = values(8).asInstanceOf[Option[String]],
          manual_resume = values(9).asInstanceOf[Boolean],
          methods = values(10).asInstanceOf[String],
          success_kw = values(11).asInstanceOf[String],
          failure_kw = values(12).asInstanceOf[String],
          filter_subject = values(13).asInstanceOf[Boolean],
          filter_body = values(14).asInstanceOf[Boolean],
          ping_url = values(15).asInstanceOf[String],
          update_url = values(16).asInstanceOf[String],
          pause_url = values(17).asInstanceOf[String],
          resume_url = values(18).asInstanceOf[String],
          channels = values(19).asInstanceOf[String],
          schedule = values(20).asInstanceOf[Option[String]],
          tz = values(21).asInstanceOf[Option[String]],
          subject = values(22).asInstanceOf[Option[String]],
          subject_fail = values(23).asInstanceOf[Option[String]],
          timeout = values(24).asInstanceOf[Option[Long]],
          last_duration = values(25).asInstanceOf[Option[Long]],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): CheckReadOnly = {
        val value =
          CheckReadOnly(
            name = values.next().asInstanceOf[String],
            slug = values.next().asInstanceOf[String],
            tags = values.next().asInstanceOf[String],
            desc = values.next().asInstanceOf[String],
            grace = values.next().asInstanceOf[Long],
            n_pings = values.next().asInstanceOf[Long],
            status = values.next().asInstanceOf[String],
            last_ping = values.next().asInstanceOf[Option[String]],
            next_ping = values.next().asInstanceOf[Option[String]],
            manual_resume = values.next().asInstanceOf[Boolean],
            methods = values.next().asInstanceOf[String],
            success_kw = values.next().asInstanceOf[String],
            failure_kw = values.next().asInstanceOf[String],
            filter_subject = values.next().asInstanceOf[Boolean],
            filter_body = values.next().asInstanceOf[Boolean],
            ping_url = values.next().asInstanceOf[String],
            update_url = values.next().asInstanceOf[String],
            pause_url = values.next().asInstanceOf[String],
            resume_url = values.next().asInstanceOf[String],
            channels = values.next().asInstanceOf[String],
            schedule = values.next().asInstanceOf[Option[String]],
            tz = values.next().asInstanceOf[Option[String]],
            subject = values.next().asInstanceOf[Option[String]],
            subject_fail = values.next().asInstanceOf[Option[String]],
            timeout = values.next().asInstanceOf[Option[Long]],
            last_duration = values.next().asInstanceOf[Option[Long]],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(name: String, slug: String, tags: String, desc: String, grace: Long, n_pings: Long, status: String, last_ping: Option[String], next_ping: Option[String], manual_resume: Boolean, methods: String, success_kw: String, failure_kw: String, filter_subject: Boolean, filter_body: Boolean, ping_url: String, update_url: String, pause_url: String, resume_url: String, channels: String, schedule: Option[String], tz: Option[String], subject: Option[String], subject_fail: Option[String], timeout: Option[Long], last_duration: Option[Long]): CheckReadOnly =
        CheckReadOnly(name, slug, tags, desc, grace, n_pings, status, last_ping, next_ping, manual_resume, methods, success_kw, failure_kw, filter_subject, filter_body, ping_url, update_url, pause_url, resume_url, channels, schedule, tz, subject, subject_fail, timeout, last_duration)
    
    }
    
    
    lazy val typeName = "CheckReadOnly"
  
  }
  
  
  
  
  trait MxCheckUpsertRequest {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[CheckUpsertRequest,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[CheckUpsertRequest,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[CheckUpsertRequest,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.name)
          .addField(_.tags)
          .addField(_.desc)
          .addField(_.timeout)
          .addField(_.grace)
          .addField(_.schedule)
          .addField(_.tz)
          .addField(_.unique)
      )
      .build
    
    implicit val zioEq: zio.prelude.Equal[CheckUpsertRequest] = zio.prelude.Equal.default
    
    implicit val catsEq: cats.Eq[CheckUpsertRequest] = cats.Eq.fromUniversalEquals
    
    lazy val generator: Generator[CheckUpsertRequest,parameters.type] =  {
      val constructors = Constructors[CheckUpsertRequest](8, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val name: CaseClassParm[CheckUpsertRequest,String] = CaseClassParm[CheckUpsertRequest,String]("name", _.name, (d,v) => d.copy(name = v), None, 0)
      lazy val tags: CaseClassParm[CheckUpsertRequest,Option[String]] = CaseClassParm[CheckUpsertRequest,Option[String]]("tags", _.tags, (d,v) => d.copy(tags = v), Some(()=> None), 1)
      lazy val desc: CaseClassParm[CheckUpsertRequest,Option[String]] = CaseClassParm[CheckUpsertRequest,Option[String]]("desc", _.desc, (d,v) => d.copy(desc = v), Some(()=> None), 2)
      lazy val timeout: CaseClassParm[CheckUpsertRequest,Option[Long]] = CaseClassParm[CheckUpsertRequest,Option[Long]]("timeout", _.timeout, (d,v) => d.copy(timeout = v), Some(()=> None), 3)
      lazy val grace: CaseClassParm[CheckUpsertRequest,Option[Long]] = CaseClassParm[CheckUpsertRequest,Option[Long]]("grace", _.grace, (d,v) => d.copy(grace = v), Some(()=> None), 4)
      lazy val schedule: CaseClassParm[CheckUpsertRequest,Option[String]] = CaseClassParm[CheckUpsertRequest,Option[String]]("schedule", _.schedule, (d,v) => d.copy(schedule = v), Some(()=> None), 5)
      lazy val tz: CaseClassParm[CheckUpsertRequest,Option[String]] = CaseClassParm[CheckUpsertRequest,Option[String]]("tz", _.tz, (d,v) => d.copy(tz = v), Some(()=> None), 6)
      lazy val unique: CaseClassParm[CheckUpsertRequest,Iterable[String]] = CaseClassParm[CheckUpsertRequest,Iterable[String]]("unique", _.unique, (d,v) => d.copy(unique = v), Some(()=> Iterable.empty), 7)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): CheckUpsertRequest = {
        CheckUpsertRequest(
          name = values(0).asInstanceOf[String],
          tags = values(1).asInstanceOf[Option[String]],
          desc = values(2).asInstanceOf[Option[String]],
          timeout = values(3).asInstanceOf[Option[Long]],
          grace = values(4).asInstanceOf[Option[Long]],
          schedule = values(5).asInstanceOf[Option[String]],
          tz = values(6).asInstanceOf[Option[String]],
          unique = values(7).asInstanceOf[Iterable[String]],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): CheckUpsertRequest = {
        val value =
          CheckUpsertRequest(
            name = values.next().asInstanceOf[String],
            tags = values.next().asInstanceOf[Option[String]],
            desc = values.next().asInstanceOf[Option[String]],
            timeout = values.next().asInstanceOf[Option[Long]],
            grace = values.next().asInstanceOf[Option[Long]],
            schedule = values.next().asInstanceOf[Option[String]],
            tz = values.next().asInstanceOf[Option[String]],
            unique = values.next().asInstanceOf[Iterable[String]],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(name: String, tags: Option[String], desc: Option[String], timeout: Option[Long], grace: Option[Long], schedule: Option[String], tz: Option[String], unique: Iterable[String]): CheckUpsertRequest =
        CheckUpsertRequest(name, tags, desc, timeout, grace, schedule, tz, unique)
    
    }
    
    
    lazy val typeName = "CheckUpsertRequest"
  
  }
}
