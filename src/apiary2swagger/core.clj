(ns apiary2swagger.core
  (:require [clojure.data.json :as json]))

(def apiary (json/read-str (slurp "src/apiary2swagger/forge.apiary.json")))

(defn strip-querystrings [path]
  (clojure.string/replace path #"\{\?.*\}" ""))

(defn parse-parameter-data [parameter method]
  {:description (get parameter "description")
   :required (get parameter "required")
;  :allowMultiple "true/false"
   :dataType (get parameter "type")
   :paramType (if (= method "GET") "query" "post")})

(defn parse-parameters [parameters method]
  (for [[paramname paramdata] parameters]
     (merge {:name paramname}
            (parse-parameter-data paramdata method))))

(defn parse-action [action]
  (let [method (get-in action ["method"])]
    {:method method
     :summary "notes here"
     :notes (get action "description")
     :responseClass "none"
     :nickname "none"
     :parameters (parse-parameters (get action "parameters") method)}))

(defn parse-resource [resource]
  {:path (strip-querystrings (get resource "uriTemplate"))
   :operations (map parse-action (get-in resource ["actions"]))})

(defn swaggerize [api]
  {:apiVersion (get-in api ["_version"] "1.0")
   :swaggerVersion "1.2"
   :basePath (get-in api ["metadata" "HOST" "value"])
   :resourcePath "/"
   :produces ["application/json"]
   :apis (map parse-resource (get-in api ["resourceGroups" 0 "resources"]))})

(spit "module" (json/write-str (swaggerize apiary)))
