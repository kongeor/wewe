(ns wewe.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [wewe.core-test]))

(doo-tests 'wewe.core-test)
