(ns scraper.core
  (:require [clj-http.client :as http]
            [cheshire.core :refer [parse-string]]))

(->> (-> "https://opploans.atlassian.net/rest/agile/1.0/board/25/backlog"
         (http/get {:query-params {:startAt 0
                                   :maxResults 9999}
                    :basic-auth ["your opploans email here"
                                 "Your api token here"]})
         :body
         (parse-string true))
     :issues
     (filter (fn [issue])))
