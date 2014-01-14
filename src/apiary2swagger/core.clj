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
  (into [](for [parameter parameters]
    (merge
      {:name (get parameter 0)}
      (parse-parameter-data (get-in parameters [(get parameter 0)]) method)))))

(defn parse-action [action]
  (let [method (get-in action ["method"])]
    {:method method
     :summary (get action "description")
     :notes nil
     :responseClass nil
     :nickname nil
     :parameters (parse-parameters (get action "parameters") method)}))

(defn parse-resource [resource]
  {:path (strip-querystrings (get resource "uriTemplate"))
   :operations (parse-actions (get-in resource ["actions" 0]))})

(defn swaggerize [api]
  {:apiVersion (get-in api ["_version"] "1.0")
   :swaggerVersion "1.2"
   :basePath (get-in api ["metadata" "HOST" "value"])
   :resourcePath "/"
   :produces ["application/json"]
   :apis (map parse-resource (get-in api ["resourceGroups" 0 "resources"]))})



; test parsing actions
(->> (get-in apiary ["resourceGroups" 0 "resources"])
    (map #(get-in % ["actions" 0]))
     (map parse-action))

(spit "apiary.swagger.json"(json/write-str (swaggerize apiary)))