(defproject jlk/time "0.1"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.joda/time "2.0"]] ;; this may be a problem later if we get to a 2.0 branch, since lein and maven seem to give them both the same name time-2.0.jar
  :plugins [[lein-swank "1.4.0"]])
