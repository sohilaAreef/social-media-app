package com.socialmedia.services;

import com.socialmedia.Enums.FriendStatus;
import com.socialmedia.dao.FriendDao;

import java.sql.SQLException;
import java.util.List;

public class FriendService {

    private final FriendDao friendDao = new FriendDao();

    public FriendStatus getStatus(int currentUserId, int otherUserId) throws SQLException {
        FriendDao.FriendStatusRow row = friendDao.findRelationship(currentUserId, otherUserId);

        if (row == null) return FriendStatus.NONE;

        if ("ACCEPTED".equals(row.status())) return FriendStatus.FRIENDS;

        if ("PENDING".equals(row.status())) {
            if (row.userId() == currentUserId && row.friendId() == otherUserId) {
                return FriendStatus.OUTGOING_PENDING;
            } else {
                return FriendStatus.INCOMING_PENDING;
            }
        }

        return FriendStatus.NONE;
    }

    public void addFriend(int currentUserId, int otherUserId) throws SQLException {
        friendDao.sendRequest(currentUserId, otherUserId);
    }

    public void cancelPending(int currentUserId, int otherUserId) throws SQLException {
        friendDao.cancelRequest(currentUserId, otherUserId);
    }

    public void acceptIncoming(int currentUserId, int otherUserId) throws SQLException {
        // incoming means other -> current
        friendDao.acceptRequest(otherUserId, currentUserId);
    }

    public void declineIncoming(int currentUserId, int otherUserId) throws SQLException {
        friendDao.declineRequest(otherUserId, currentUserId);
    }

    public void unfriend(int currentUserId, int otherUserId) throws SQLException {
        friendDao.unfriend(currentUserId, otherUserId);
    }

    public List<FriendDao.UserMini> listFriends(int currentUserId) throws SQLException {
        return friendDao.getFriends(currentUserId);
    }

    public List<FriendDao.UserMini> listIncoming(int currentUserId) throws SQLException {
        return friendDao.getIncomingRequests(currentUserId);
    }

    public List<FriendDao.UserMini> listOutgoing(int currentUserId) throws SQLException {
        return friendDao.getOutgoingRequests(currentUserId);
    }
}