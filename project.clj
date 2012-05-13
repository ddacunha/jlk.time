(defproject jlk/time "0.1-SNAPSHOT"
  :description "time functions"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.joda/time "2.0"] ;; this may be a problem later if we get to a 2.0 branch, since lein and maven seem to give them both the same name time-2.0.jar
                 [jlk/utility "0.1"]]
  :plugins [[lein-swank "1.4.0"]])
