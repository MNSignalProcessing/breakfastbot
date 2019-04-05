(ns breakfastbot.announcement-test
  (:require [breakfastbot.announcement :as sut]
            [clojure.string :as s]
            [clojure.test :as t]))

(def mock-data
  {:bringer  {:fullname "Jimmy McGill"}
   :attendees [{:fullname "Jimmy McGill"}
               {:fullname "Joey Dixon"}
               {:fullname "Drama Girl"}
               {:fullname "Sound Guy"}]})

(t/deftest announcement
  (t/testing "is correctly formated"
    (t/is (= (s/join "\n" ["🤖📣 BREAKFAST SCHEDULED 🤖📣"
                           ""
                           "Attendees:"
                           "* @**Jimmy McGill**"
                           "* @**Joey Dixon**"
                           "* @**Drama Girl**"
                           "* @**Sound Guy**"
                           "Total attendees: 4"
                           ""
                           "Responsible for bringing Breakfast: @**Jimmy McGill**"])
             (sut/announce-breakfast-message mock-data)))))
