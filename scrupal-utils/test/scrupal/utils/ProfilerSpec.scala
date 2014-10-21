package scrupal.utils

import org.specs2.mutable.Specification

/** Test Suite For Profiler
 */
class ProfilerSpec extends Specification {

  "Profiler" should {
    "Provide basic profiling" in {
      Profiler.profiling_enabled = true
      val result = Profiler.profile("Profiler Test Case") {
        42
      }
      result must beEqualTo(42)
      val sb = Profiler.format_profile_data.toString()
      sb.contains("Profiler Test Case") must beTrue
    }
  }
}
