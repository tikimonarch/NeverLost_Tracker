package com.example.tracker1.Model;

import java.util.List;

public class MyResponse {
    public long multicast_id;
    public int success, failure, canonical_ids;
    public List<Result> result;
}
