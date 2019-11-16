(ns wewe.core-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [wewe.core :as core]))

(deftest fake-test
  (testing "fake description"
    (is (= 1 2))))
