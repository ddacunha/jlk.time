(ns jlk.time.core
  (:refer-clojure :exclude [format time])
  (use [jlk.utility.core :only [exception]])
  (import [java.lang IllegalArgumentException]
	  [org.joda.time MutableDateTime LocalDateTime]
	  [org.joda.time.format DateTimeFormat DateTimeFormatter ISODateTimeFormat]))
;;
;; this is going back to be a thin wrapper around joda time
;; there is some new stuff in clojure 1.4 to do with time, look into this
;;

(defmulti formatter
  "format according to definition s or keyword, :iso-date :iso-time, :iso, :iso-nomillis
source: joda time api
 G       era                          text          AD
 C       century of era (>=0)         number        20
 Y       year of era (>=0)            year          1996

 x       weekyear                     year          1996
 w       week of weekyear             number        27
 e       day of week                  number        2
 E       day of week                  text          Tuesday; Tue

 y       year                         year          1996
 D       day of year                  number        189
 M       month of year                month         July; Jul; 07
 d       day of month                 number        10

 a       halfday of day               text          PM
 K       hour of halfday (0~11)       number        0
 h       clockhour of halfday (1~12)  number        12

 H       hour of day (0~23)           number        0
 k       clockhour of day (1~24)      number        24
 m       minute of hour               number        30
 s       second of minute             number        55
 S       fraction of second           number        978

 z       time zone                    text          Pacific Standard Time; PST
 Z       time zone offset/id          zone          -0800; -08:00; America/Los_Angeles

 '       escape for text              delimiter
 ''      single quote                 literal       '"
  (fn [x] (if (instance? String x)
            :string
            x)))
(def fmt formatter)

(defmethod formatter :string
  [s]
  (DateTimeFormat/forPattern s))

(defmethod formatter :basic-date
  [_]
  (ISODateTimeFormat/basicDate))

(defmethod formatter :basic-time
  [_]
  (ISODateTimeFormat/basicTimeNoMillis))

(defmethod formatter :basic-time-long
  [_]
  (ISODateTimeFormat/basicTime))

(defmethod formatter :iso
  [_]
  (ISODateTimeFormat/dateTime))

(defmethod formatter :iso-nomillis
  [_]
  (ISODateTimeFormat/dateTimeNoMillis))

(defmethod formatter :iso-date
  [_]
  (formatter "YYYY-MM-dd"))

(defmethod formatter :iso-time
  [_]
  (formatter "HH:mm:ssZ"))

(defmethod formatter :iso-time-long
  [_]
  (formatter "HH:mm:ss.SSSZ"))

(defmethod formatter :short
  [_]
  (DateTimeFormat/shortDateTime))

(defmethod formatter :medium
  [_]
  (DateTimeFormat/mediumDateTime))

(defmethod formatter :long
  [_]
  (DateTimeFormat/longDateTime))

(defmulti convert
  "convert date between formats.  eg (convert (now) (fmt :iso)"
  (fn [_ to-format] (if (keyword? to-format)
                      to-format
                      :format))) ;; should check if it is a formatter first

(defmethod convert :java
  [t _]
  (java.util.Date. (.getMillis t)))

(defmethod convert :long
  [t _]
  (.getMillis t))

(defmethod convert :sql-date
  [t _]
  (java.sql.Date. (.getMillis t)))

(defmethod convert :sql-time
  [t _]
  (java.sql.Time. (.getMillis t)))

(defmethod convert :sql-timestamp
  [t _]
  (java.sql.Timestamp. (.getMillis t)))

(defmethod convert :format
  [t f]
  (.print f t))

;; (defmethod convert :default
;;   [_ _]
;;   (exception "no valid conversion"))

(defn now
  []
  (org.joda.time.DateTime.))
(defn timestamp
  ([]
     (convert (now) :long))
  ([t]
     (convert t :long)))

(defn parse
  [s formatter]
  (.parseDateTime formatter s))
(def time parse)
