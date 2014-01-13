(ns apiary2swagger.core
  (:require [clojure.data.json :as json]))

(def apiary (json/read-str (slurp "src/apiary2swagger/forge.apiary.json")))





