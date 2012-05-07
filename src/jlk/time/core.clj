(ns jlk.time.core
  (:refer-clojure :exclude [format time])
  (import [java.lang IllegalArgumentException]
	  [org.joda.time MutableDateTime LocalDateTime]
	  [org.joda.time.format DateTimeFormat DateTimeFormatter ISODateTimeFormat]))

;;
;; i'm going to try define a purely clojure time type and use joda to handle time complexities
;; -- see functions near the end
;;

;;
;; joda library would appear to be a good match for clojure since it primarily
;; uses immutable data objects
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

;; NYI - default parsing types
;;     - i'm not actually sure they *should* be implemented
;;
;; (def ^:dynamic *formatters*
;;   "formatters used by parse/format functions"
;;   nil)

;;
;; parsing only occurs using strings and formatters
;;

(defn parse
  ([s]
     (throw (Exception. "NYI - default parsing types")))
  ([s formatter]
     (.parseDateTime formatter s)))

(defn string
  ([d]
     (throw (Exception. "NYI - default parsing types")))
  ([d formatter]
     (.print formatter d)))

;; (defn to-timestamp
;;   [t]
;;   (java.sql.Timestamp. (.getMillis t)))

;;
;; time type in jlk.common
;;

;; time objects keep track of how they are formatted...
;; valid formats are:
;;   - :keyword/string (see above)
;;   - :timestamp/long
;;   - :joda/org.joda.time.DateTime
;;   - :sql-timestamp/java.sql.Timestamp
(defrecord Time [format value])

(defmulti to-joda
  "assume that we are being given a jlk.time.Time"
  :format)
(defmethod to-joda :joda
  [{:keys [value]}]
  value)
(defmethod to-joda :timestamp
  [{:keys [value]}]
  (org.joda.time.DateTime. value))
(defmethod to-joda :sql-timestamp
  [{:keys [value]}]
  (org.joda.time.DateTime. (.getTime value)))
(defmethod to-joda :sql-date
  [{:keys [value]}]
  (org.joda.time.DateTime. (.getTime value)))
(defmethod to-joda :sql-time
  [{:keys [value]}]
  (org.joda.time.DateTime. (.getTime value)))
(defmethod to-joda :java-date
  [{:keys [value]}]
  (org.joda.time.DateTime. (.getTime value)))
(defmethod to-joda :default
  [{:keys [format value]}]
  (parse value (formatter format)))

(defmulti to-time
  "assume that we are being given a joda DateTime"
  (fn [d format] format))
(defmethod to-time :joda
  [d _]
  (Time. :joda d))
(defmethod to-time :timestamp
  [d _]
  (Time. :timestamp (.getMillis d)))
(defmethod to-time :sql-timestamp
  [d _]
  (Time. :sql-timestamp (java.sql.Timestamp. (.getMillis d))))
(defmethod to-time :sql-date
  [d _]
  (Time. :sql-date (java.sql.Date. (.getMillis d))))
(defmethod to-time :sql-time
  [d _]
  (Time. :sql-time (java.sql.Time. (.getMillis d))))
(defmethod to-time :java-date
  [d _]
  (Time. :java-date (java.util.Date. (.getMillis d))))
(defmethod to-time :default
  [d format]
  (Time. format (string d (formatter format))))

;;
;; define a how to make a Time
;; the following are more 'user' functions
;;
(defn now
  [format]
  (to-time (org.joda.time.DateTime.) format))

(defn convert
  [t to-format]
  (to-time (to-joda t) to-format))

(defn time
  "this is like parse, but returns a jlk.time.Time"
  ([s]
     (throw (Exception. "NYI - default parsing types")))
  ([s format]
     (to-time (parse s (formatter format)) format))
  ([s input-format output-format]
     (to-time (parse s (formatter input-format)) output-format)))

(defn timestamp
  "this accepts a long (long)"
  ([l] (to-time (org.joda.time.DateTime. l) :timestamp))
  ([l format] (to-time (org.joda.time.DateTime. l) format)))
