<?php
$linjer = file(__DIR__ . '/../.env', FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES);
foreach ($linjer as $linje) {
    [$navn, $verdi] = explode('=', $linje, 2);
        define(trim($navn), trim($verdi));
}