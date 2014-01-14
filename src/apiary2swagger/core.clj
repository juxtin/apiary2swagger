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
   :paramType (if (= method "GET") "query" "post")}
  )

(defn parse-parameters [parameters method]
  (into [](for [parameter parameters]
    (merge
      {:name (get parameter 0)}
      (parse-parameter-data (get-in parameters [(get parameter 0)]) method)))))

(defn parse-actions [actions]
  (let [method (get-in actions [0 "method"])]
    {:method method
     :summary "search for modules"
     :notes " "
     :responseClass " "
     :nickname " "
     :parameters (get actions "parameters")
     })
  )

(defn parse-resource-groups [resourceGroups]
  {:path (strip-querystrings (get-in resourceGroups [0 "resources" 0 "uriTemplate"]))
   :operations (parse-actions (get resourceGroups "actions"))})



(defn swaggerize [api]
  {:apiVersion (get-in api ["_version"] "1.0")
   :swaggerVersion "1.2"
   :basePath (get-in api ["metadata" "HOST" "value"])
   :resourcePath "/"
   :produces ["application/json"]
   :apis [(parse-resource-groups (get-in api ["resourceGroups"]))]
   }
  )

(parse-parameters (get-in apiary ["resourceGroups" 0 "resources" 0 "actions" 0 "parameters"]) "GET")


(swaggerize apiary)